package com.ajit.springAI.VideoGenaration.service;

import com.ajit.springAI.VideoGenaration.model.AudioArtifact;
import com.ajit.springAI.VideoGenaration.model.ContentBrief;
import com.ajit.springAI.VideoGenaration.model.GenerationStatus;
import com.ajit.springAI.VideoGenaration.model.GeneratedImage;
import com.ajit.springAI.VideoGenaration.model.MediaJob;
import com.ajit.springAI.VideoGenaration.model.MediaJobStatus;
import com.ajit.springAI.VideoGenaration.model.Narration;
import com.ajit.springAI.VideoGenaration.model.PanelPrompt;
import com.ajit.springAI.VideoGenaration.model.ProjectRecord;
import com.ajit.springAI.VideoGenaration.model.ProjectStatus;
import com.ajit.springAI.VideoGenaration.model.RevisionRequest;
import com.ajit.springAI.VideoGenaration.model.Scene;
import com.ajit.springAI.VideoGenaration.model.ScriptDraft;
import com.ajit.springAI.VideoGenaration.model.VideoOutputMetadata;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VideoGenerationService {

    private static final int MAX_CONCURRENT_JOBS = 3;
    private static final int MAX_JOBS_PER_PROJECT_PER_HOUR = 5;

    private final ChatClient chatClient;
    private final ImageGenerator imageGenerator;
    private final SpeechGenerator speechGenerator;
    private final ExecutorService generationExecutor = Executors.newFixedThreadPool(2);
    private final AtomicInteger activeJobs = new AtomicInteger();
    private final Map<String, MediaJob> jobs = new ConcurrentHashMap<>();
    private final Map<String, Deque<Instant>> projectJobHistory = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public VideoGenerationService() {
        this(null, new TemplateImageGenerator(), new CaptionSpeechGenerator());
    }

    public VideoGenerationService(ChatClient chatClient) {
        this(chatClient, new TemplateImageGenerator(), new CaptionSpeechGenerator());
    }

    @Autowired
    public VideoGenerationService(ChatClient chatClient, ImageGenerator imageGenerator,
            SpeechGenerator speechGenerator) {
        this.chatClient = chatClient;
        this.imageGenerator = imageGenerator != null ? imageGenerator : new TemplateImageGenerator();
        this.speechGenerator = speechGenerator != null ? speechGenerator : new CaptionSpeechGenerator();
    }

    public VideoGenerationService(ChatClient chatClient, ImageGenerator imageGenerator) {
        this(chatClient, imageGenerator, new CaptionSpeechGenerator());
    }

    public MediaJob enqueueVideoGeneration(ProjectRecord project, ScriptDraft approvedDraft) {
        if (project == null || approvedDraft == null) {
            throw new IllegalArgumentException("Project and approved draft are required.");
        }
        enforceProjectRateLimit(project.getId());
        if (activeJobs.get() >= MAX_CONCURRENT_JOBS) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Too many active jobs. Retry later.");
        }
        if (activeJobs.incrementAndGet() > MAX_CONCURRENT_JOBS) {
            activeJobs.decrementAndGet();
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Too many active jobs. Retry later.");
        }

        MediaJob mediaJob = new MediaJob();
        mediaJob.setId(UUID.randomUUID().toString());
        mediaJob.setProjectId(project.getId());
        mediaJob.setVersionNumber(approvedDraft.getVersionNumber());
        mediaJob.setStatus(MediaJobStatus.QUEUED);
        mediaJob.setProgress(0);
        mediaJob.setMessage("Queued for rendering");
        mediaJob.setTimeoutSeconds(180);
        jobs.put(mediaJob.getId(), mediaJob);

        generationExecutor.submit(() -> runGenerationJob(project, approvedDraft, mediaJob));
        return mediaJob;
    }

    public List<MediaJob> getJobsForProject(String projectId) {
        return jobs.values().stream()
                .filter(job -> projectId.equals(job.getProjectId()))
                .sorted(Comparator.comparing(MediaJob::getCreatedAt).reversed())
                .toList();
    }

    public MediaJob getJob(String jobId) {
        MediaJob job = jobs.get(jobId);
        if (job == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found: " + jobId);
        }
        return job;
    }

    public MediaJob cancelJob(String jobId) {
        MediaJob mediaJob = getJob(jobId);
        if (mediaJob.getStatus() == MediaJobStatus.COMPLETED || mediaJob.getStatus() == MediaJobStatus.FAILED
                || mediaJob.getStatus() == MediaJobStatus.CANCELLED) {
            return mediaJob;
        }
        mediaJob.requestCancellation();
        mediaJob.setStatus(MediaJobStatus.CANCELLED);
        mediaJob.setMessage("Render cancelled by the user.");
        cleanupJobFiles(mediaJob);
        return mediaJob;
    }

    public String generateText(String prompt) {
        if (chatClient == null) {
            return "Local demo mode active. Prompt: " + prompt;
        }
        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    public ScriptDraft generateScript(ContentBrief brief) {
        String prompt = buildScriptPrompt(brief);
        String content = generateText(prompt);
        try {
            String cleanedJson = extractJson(content);
            return objectMapper.readValue(cleanedJson, ScriptDraft.class);
        } catch (JsonProcessingException e) {
            ScriptDraft fallback = new ScriptDraft();
            fallback.setTitle("Video Script");
            fallback.setHook("A strong opening statement for your topic.");
            fallback.setNarration(brief.getTopic());
            fallback.setStatus("generated");
            fallback.setLanguage(brief.getLanguage());
            List<Scene> scenes = new ArrayList<>();
            Scene scene = new Scene();
            scene.setSequence(1);
            scene.setVisual(brief.getTopic());
            scene.setNarration(brief.getTopic());
            scene.setDuration(brief.getDuration());
            scenes.add(scene);
            fallback.setScenes(scenes);
            return fallback;
        }
    }

    public ScriptDraft reviseScript(ScriptDraft current, RevisionRequest revisionRequest) {
        String prompt = "You are revising a video script. Keep the structure, improve it using the feedback, and return strict JSON with title, hook, narration, status, language, and scenes[] where each scene has sequence, visual, narration and duration. Current script: "
                + objectToJson(current)
                + "\nUser feedback: " + revisionRequest.getFeedback();

        String content = generateText(prompt);
        try {
            String cleanedJson = extractJson(content);
            ScriptDraft revised = objectMapper.readValue(cleanedJson, ScriptDraft.class);
            revised.setStatus("revised");
            return revised;
        } catch (JsonProcessingException e) {
            ScriptDraft fallback = copyCurrentWithStatus(current, "revised");
            fallback.setNarration(revisionRequest.getFeedback());
            return fallback;
        }
    }

    public String improveScene(ScriptDraft currentDraft, int sceneIndex, String sceneText, String improvementRequest) {
        if (currentDraft == null || sceneText == null || sceneText.isBlank()) {
            return "";
        }

        String prompt = "Improve this specific video scene. Keep the meaning, make it more polished, and follow this request: "
                + improvementRequest
                + ". Return only the improved scene text. Scene text:\n"
                + sceneText;

        String improvement = generateText(prompt);
        return improvement == null ? sceneText : improvement.trim();
    }

    public VideoOutputMetadata renderApprovedVersion(ProjectRecord project, ScriptDraft approvedDraft) {
        return renderApprovedVersion(project, approvedDraft, null, 1);
    }

    private void runGenerationJob(ProjectRecord project, ScriptDraft approvedDraft, MediaJob mediaJob) {
        try {
            mediaJob.setStatus(MediaJobStatus.RUNNING);
            mediaJob.setProgress(5);
            mediaJob.setMessage("Preparing scene frames and audio");
            VideoOutputMetadata output = renderApprovedVersion(project, approvedDraft, mediaJob, 1);
            if (mediaJob.isCancelled()) {
                throw new InterruptedException("Render cancelled before completion");
            }
            mediaJob.setStatus(MediaJobStatus.COMPLETED);
            mediaJob.setProgress(100);
            mediaJob.setMessage("Render completed successfully.");
            mediaJob.addOutputFile(output.getOutputPath());
            project.setStatus(ProjectStatus.COMPLETED);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            mediaJob.setStatus(MediaJobStatus.CANCELLED);
            mediaJob.setProgress(100);
            mediaJob.setMessage("Render cancelled by the user.");
            project.setStatus(ProjectStatus.FAILED);
            cleanupJobFiles(mediaJob);
        } catch (CancellationException e) {
            mediaJob.setStatus(MediaJobStatus.CANCELLED);
            mediaJob.setProgress(100);
            mediaJob.setMessage(e.getMessage() == null ? "Render cancelled by the user." : e.getMessage());
            mediaJob.setErrorMessage(e.getMessage());
            project.setStatus(ProjectStatus.FAILED);
            cleanupProjectArtifacts(project, approvedDraft.getVersionNumber());
            cleanupJobFiles(mediaJob);
        } catch (Exception e) {
            String reason = e.getMessage() == null ? "Unknown render error" : e.getMessage();
            mediaJob.setStatus(MediaJobStatus.FAILED);
            mediaJob.setProgress(100);
            mediaJob.setErrorMessage(reason);
            mediaJob.setMessage("Render failed: " + reason);
            project.setStatus(ProjectStatus.FAILED);
            cleanupProjectArtifacts(project, approvedDraft.getVersionNumber());
            cleanupJobFiles(mediaJob);
        } finally {
            activeJobs.decrementAndGet();
        }
    }

    private VideoOutputMetadata renderApprovedVersion(ProjectRecord project, ScriptDraft approvedDraft,
            MediaJob mediaJob, int attempt) {
        if (project == null || approvedDraft == null) {
            throw new IllegalArgumentException("Project and approved draft are required.");
        }
        if (mediaJob != null && mediaJob.isCancelled()) {
            throw new CancellationException("Render cancelled by the user.");
        }

        Integer requestedVersion = approvedDraft.getVersionNumber() != null
                ? approvedDraft.getVersionNumber()
                : project.getApprovedVersion() != null ? project.getApprovedVersion() : 1;

        Path outputRoot = Paths.get("generated", "outputs", project.getId(), "version-" + requestedVersion);
        try {
            Files.createDirectories(outputRoot);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create output directory for project " + project.getId(), e);
        }

        List<String> frameFiles = new ArrayList<>();
        StringBuilder subtitles = new StringBuilder();
        int startMs = 0;
        List<Scene> scenes = approvedDraft.getScenes() == null ? List.of() : approvedDraft.getScenes();

        for (int i = 0; i < scenes.size(); i++) {
            if (mediaJob != null && mediaJob.isCancelled()) {
                throw new CancellationException("Render cancelled by the user.");
            }
            Scene scene = scenes.get(i);
            String visualPrompt = scene.getVisual() != null && !scene.getVisual().isBlank()
                    ? scene.getVisual()
                    : (scene.getNarration() != null ? scene.getNarration() : "Scene " + (i + 1));
            String narrationText = scene.getNarration() != null && !scene.getNarration().isBlank()
                    ? scene.getNarration()
                    : visualPrompt;
            int durationMs = parseDuration(scene.getDuration(), 5000);

            if (mediaJob != null) {
                int progress = 15 + (int) (((double) (i + 1) / Math.max(1, scenes.size())) * 60);
                mediaJob.setProgress(progress);
                mediaJob.setMessage("Generating scene " + (i + 1) + " of " + scenes.size());
            }

            GeneratedImage generatedImage = imageGenerator.generate(new PanelPrompt(visualPrompt, "comic"));
            if (generatedImage != null && generatedImage.getImageUrl() != null) {
                frameFiles.add(generatedImage.getImageUrl());
            }

            AudioArtifact audioArtifact = speechGenerator.synthesize(new Narration(narrationText));
            String subtitleText = audioArtifact != null && audioArtifact.getCaptionUrl() != null
                    ? audioArtifact.getCaptionUrl()
                    : narrationText;

            subtitles.append(formatSrt(i + 1, startMs, startMs + durationMs, subtitleText))
                    .append(System.lineSeparator());
            startMs += durationMs;
        }

        if (frameFiles.isEmpty()) {
            Path fallbackPanel = outputRoot.resolve("fallback-panel.png");
            GeneratedImage fallback = imageGenerator.generate(new PanelPrompt("Fallback scene", "comic"));
            if (fallback != null && fallback.getImageUrl() != null) {
                frameFiles.add(fallback.getImageUrl());
            }
            if (frameFiles.isEmpty()) {
                try {
                    Files.writeString(fallbackPanel, "Fallback panel", StandardCharsets.UTF_8);
                    frameFiles.add(fallbackPanel.toString());
                } catch (IOException ignored) {
                    // no-op fallback
                }
            }
        }

        Path subtitlesFile = outputRoot.resolve("captions.srt");
        try {
            Files.writeString(subtitlesFile, subtitles.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Could not write captions for project " + project.getId(), e);
        }

        Path videoPath = outputRoot.resolve("demo-output.mp4");
        boolean rendered = renderVideoSequence(frameFiles, videoPath, mediaJob);

        VideoOutputMetadata metadata = new VideoOutputMetadata();
        metadata.setProjectId(project.getId());
        metadata.setApprovedScriptVersion(requestedVersion);
        metadata.setProvider("local-template");
        metadata.setModel("template-demo");
        metadata.setOutputPath(videoPath.toString());
        metadata.setMetadataPath(outputRoot.resolve("metadata.json").toString());
        metadata.setStatus(rendered ? GenerationStatus.COMPLETED : GenerationStatus.FAILED);
        metadata.setCreatedAt(Instant.now());

        project.setStatus(rendered ? ProjectStatus.COMPLETED : ProjectStatus.FAILED);
        project.addGeneratedOutput(metadata);

        try {
            objectMapper.writeValue(outputRoot.resolve("metadata.json").toFile(), metadata);
        } catch (IOException e) {
            throw new IllegalStateException("Could not persist video metadata for project " + project.getId(), e);
        }

        if (mediaJob != null) {
            mediaJob.setProgress(95);
            mediaJob.setMessage("Writing final metadata file");
        }

        if (!rendered) {
            throw new IllegalStateException("Video render failed for project " + project.getId() + ".");
        }

        return metadata;
    }

    private boolean renderVideoSequence(List<String> frameFiles, Path outputPath, MediaJob mediaJob) {
        if (frameFiles == null || frameFiles.isEmpty()) {
            return false;
        }

        int maxAttempts = mediaJob != null ? mediaJob.getMaxAttempts() : 2;
        long timeoutSeconds = mediaJob != null ? mediaJob.getTimeoutSeconds() : 120L;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            if (mediaJob != null && mediaJob.isCancelled()) {
                throw new CancellationException("Render cancelled by the user.");
            }
            try {
                createSequenceFile(frameFiles, outputPath);
                List<String> command = new ArrayList<>();
                command.add("ffmpeg");
                command.add("-y");
                command.add("-f");
                command.add("concat");
                command.add("-safe");
                command.add("0");
                command.add("-i");
                command.add(outputPath.getParent().resolve("panel-sequence.txt").toString());
                command.add("-vf");
                command.add(
                        "fps=1/3,scale=1280:720:force_original_aspect_ratio=decrease,pad=1280:720:(ow-iw)/2:(oh-ih)/2,format=yuv420p");
                command.add("-c:v");
                command.add("libx264");
                command.add("-pix_fmt");
                command.add("yuv420p");
                command.add(outputPath.toString());

                if (mediaJob != null) {
                    mediaJob.setAttempts(attempt);
                    mediaJob.setMessage("Encoding final video (attempt " + attempt + "/" + maxAttempts + ")");
                }

                Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
                boolean completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
                if (!completed) {
                    process.destroyForcibly();
                    throw new IllegalStateException("FFmpeg timed out after " + timeoutSeconds + " seconds.");
                }
                int exitCode = process.exitValue();
                if (exitCode == 0 && Files.exists(outputPath)) {
                    return true;
                }
                if (attempt < maxAttempts) {
                    cleanupPath(outputPath);
                    cleanupPath(outputPath.getParent().resolve("panel-sequence.txt"));
                    continue;
                }
                throw new IllegalStateException("FFmpeg exited with code " + exitCode + ".");
            } catch (IOException | InterruptedException e) {
                Thread.currentThread().interrupt();
                if (attempt >= maxAttempts) {
                    try {
                        Files.writeString(outputPath, "Demo video placeholder. Install FFmpeg for real export.",
                                StandardCharsets.UTF_8);
                        return true;
                    } catch (IOException ignored) {
                        return false;
                    }
                }
            } catch (IllegalStateException e) {
                if (attempt >= maxAttempts) {
                    return false;
                }
            }
        }
        return false;
    }

    private void createSequenceFile(List<String> frameFiles, Path outputPath) throws IOException {
        Path sequenceFile = outputPath.getParent().resolve("panel-sequence.txt");
        StringBuilder concat = new StringBuilder();
        for (String framePath : frameFiles) {
            String normalized = framePath.replace('\\', '/');
            concat.append("file '").append(normalized).append("'\n");
        }
        Files.writeString(sequenceFile, concat.toString(), StandardCharsets.UTF_8);
    }

    private void cleanupJobFiles(MediaJob mediaJob) {
        if (mediaJob == null) {
            return;
        }
        for (String outputFile : mediaJob.getOutputFiles()) {
            cleanupPath(Path.of(outputFile));
        }
        if (StringUtils.hasText(mediaJob.getProjectId())) {
            cleanupProjectArtifacts(mediaJob.getProjectId());
        }
    }

    private void cleanupProjectArtifacts(ProjectRecord project, Integer versionNumber) {
        if (project == null || !StringUtils.hasText(project.getId())) {
            return;
        }
        Path root = Paths.get("generated", "outputs", project.getId());
        if (versionNumber != null) {
            cleanupPath(root.resolve("version-" + versionNumber));
        } else {
            cleanupPath(root);
        }
    }

    private void cleanupProjectArtifacts(String projectId) {
        if (!StringUtils.hasText(projectId)) {
            return;
        }
        cleanupPath(Paths.get("generated", "outputs", projectId));
    }

    private void cleanupPath(Path path) {
        if (path == null || !Files.exists(path)) {
            return;
        }
        try {
            if (Files.isDirectory(path)) {
                try (var stream = Files.walk(path)) {
                    stream.sorted(Comparator.reverseOrder())
                            .forEach(p -> {
                                try {
                                    Files.deleteIfExists(p);
                                } catch (IOException ignored) {
                                    // no-op cleanup
                                }
                            });
                }
            } else {
                Files.deleteIfExists(path);
            }
        } catch (IOException ignored) {
            // no-op cleanup
        }
    }

    private void enforceProjectRateLimit(String projectId) {
        if (!StringUtils.hasText(projectId)) {
            return;
        }
        Deque<Instant> history = projectJobHistory.computeIfAbsent(projectId, key -> new ArrayDeque<>());
        Instant windowStart = Instant.now().minus(Duration.of(1, ChronoUnit.HOURS));
        while (!history.isEmpty() && history.peekFirst().isBefore(windowStart)) {
            history.pollFirst();
        }
        if (history.size() >= MAX_JOBS_PER_PROJECT_PER_HOUR) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Rate limit reached for this project. Please wait before starting another render.");
        }
        history.addLast(Instant.now());
    }

    private int parseDuration(String rawDuration, int defaultMs) {
        if (rawDuration == null || rawDuration.isBlank()) {
            return defaultMs;
        }
        String sanitized = rawDuration.trim().toLowerCase();
        if (sanitized.endsWith("s")) {
            try {
                return Math.max(1000, (int) (Double.parseDouble(sanitized.replace("s", "")) * 1000));
            } catch (NumberFormatException ignored) {
                return defaultMs;
            }
        }
        if (sanitized.endsWith("ms")) {
            try {
                return Math.max(1000, Integer.parseInt(sanitized.replace("ms", "")));
            } catch (NumberFormatException ignored) {
                return defaultMs;
            }
        }
        return defaultMs;
    }

    private String formatSrt(int index, int startMs, int endMs, String caption) {
        return index + System.lineSeparator()
                + formatTimestamp(startMs) + " --> " + formatTimestamp(endMs) + System.lineSeparator()
                + caption + System.lineSeparator();
    }

    private String formatTimestamp(int ms) {
        int totalSeconds = ms / 1000;
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        int milliseconds = ms % 1000;
        return String.format("%02d:%02d:%02d,%03d", hours, minutes, seconds, milliseconds);
    }

    private String buildScriptPrompt(ContentBrief brief) {
        return "Create a short marketing video script in JSON only. "
                + "Fields: title, hook, narration, status, language, scenes[] where each scene has sequence, visual, narration, duration. "
                + "Topic: " + brief.getTopic() + ". Audience: " + brief.getAudience() + ". Tone: " + brief.getTone()
                + ". Duration: " + brief.getDuration() + ". Language: " + brief.getLanguage()
                + ". Style: " + (brief.getStyle() == null ? "cinematic" : brief.getStyle())
                + ". Return valid JSON without markdown fences.";
    }

    private String extractJson(String content) {
        if (content == null) {
            return "{}";
        }
        String cleaned = content.trim();
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return cleaned.substring(start, end + 1);
        }
        return cleaned;
    }

    private String objectToJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return value == null ? "{}" : value.toString();
        }
    }

    private ScriptDraft copyCurrentWithStatus(ScriptDraft current, String status) {
        ScriptDraft copy = new ScriptDraft();
        copy.setTitle(current.getTitle());
        copy.setHook(current.getHook());
        copy.setNarration(current.getNarration());
        copy.setScenes(current.getScenes() == null ? new ArrayList<>() : current.getScenes());
        copy.setStatus(status);
        copy.setLanguage(current.getLanguage());
        return copy;
    }
}
