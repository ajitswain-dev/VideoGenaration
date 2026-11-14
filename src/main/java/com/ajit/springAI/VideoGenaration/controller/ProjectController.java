package com.ajit.springAI.VideoGenaration.controller;

import com.ajit.springAI.VideoGenaration.model.ContentBrief;
import com.ajit.springAI.VideoGenaration.model.CreateProjectRequest;
import com.ajit.springAI.VideoGenaration.model.ProjectRecord;
import com.ajit.springAI.VideoGenaration.model.RevisionRequest;
import com.ajit.springAI.VideoGenaration.model.SceneImprovementRequest;
import com.ajit.springAI.VideoGenaration.model.SceneImprovementResponse;
import com.ajit.springAI.VideoGenaration.model.ScriptDraft;
import com.ajit.springAI.VideoGenaration.service.VideoGenerationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class ProjectController {

    private final VideoGenerationService videoGenerationService;
    private final Map<String, ProjectRecord> projects = new ConcurrentHashMap<>();

    public ProjectController(VideoGenerationService videoGenerationService) {
        this.videoGenerationService = videoGenerationService;
    }

    @PostMapping("/projects")
    public ResponseEntity<ProjectRecord> createProject(@Valid @RequestBody CreateProjectRequest request) {
        validateCreateRequest(request);

        String projectId = UUID.randomUUID().toString();
        String projectName = StringUtils.hasText(request.getProjectName())
                ? request.getProjectName()
                : (StringUtils.hasText(request.getTopic()) ? request.getTopic() : "Untitled Project");

        ProjectRecord project = new ProjectRecord(
                projectId,
                projectName,
                request.getTopic(),
                request.getAudience(),
                request.getTone(),
                request.getDuration(),
                request.getLanguage(),
                request.getStyle());

        projects.put(projectId, project);
        return ResponseEntity.status(HttpStatus.CREATED).body(project);
    }

    @PostMapping("/projects/{id}/draft")
    public ResponseEntity<ScriptDraft> generateDraft(@PathVariable String id, @Valid @RequestBody ContentBrief brief) {
        ProjectRecord project = requireProject(id);
        validateBrief(brief);

        ScriptDraft generated = videoGenerationService.generateScript(brief);
        generated.setStatus("generated");
        generated.setLanguage(brief.getLanguage());

        project.addVersion(generated);
        return ResponseEntity.ok(generated);
    }

    @GetMapping("/projects/{id}")
    public ResponseEntity<ProjectRecord> getProject(@PathVariable String id) {
        return ResponseEntity.ok(requireProject(id));
    }

    @GetMapping("/projects/{id}/versions")
    public ResponseEntity<List<ScriptDraft>> getVersions(@PathVariable String id) {
        ProjectRecord project = requireProject(id);
        return ResponseEntity.ok(project.getVersions());
    }

    @PostMapping("/projects/{id}/revise")
    public ResponseEntity<ScriptDraft> reviseProject(@PathVariable String id,
            @Valid @RequestBody RevisionRequest revisionRequest) {
        ProjectRecord project = requireProject(id);
        if (project.getCurrentDraft() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project has no existing draft to revise.");
        }

        ScriptDraft revisedDraft = videoGenerationService.reviseScript(project.getCurrentDraft(), revisionRequest);
        revisedDraft.setStatus("revised");
        project.addVersion(revisedDraft);
        return ResponseEntity.ok(revisedDraft);
    }

    @PostMapping("/projects/{id}/scene-improve")
    public ResponseEntity<SceneImprovementResponse> improveScene(@PathVariable String id,
            @RequestBody SceneImprovementRequest request) {
        ProjectRecord project = requireProject(id);
        if (project.getCurrentDraft() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project has no existing draft to improve.");
        }
        if (request == null || !StringUtils.hasText(request.getImprovementRequest())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "improvementRequest is required.");
        }

        String improvedText = videoGenerationService.improveScene(
                project.getCurrentDraft(),
                request.getSceneIndex(),
                request.getSceneText(),
                request.getImprovementRequest());

        SceneImprovementResponse response = new SceneImprovementResponse();
        response.setSceneIndex(request.getSceneIndex());
        response.setImprovedText(improvedText);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/editor")
    public String editorPage() {
        return "forward:/editor.html";
    }

    private ProjectRecord requireProject(String id) {
        ProjectRecord project = projects.get(id);
        if (project == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found: " + id);
        }
        return project;
    }

    private void validateCreateRequest(CreateProjectRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required.");
        }
        if (!StringUtils.hasText(request.getTopic())
                || !StringUtils.hasText(request.getAudience())
                || !StringUtils.hasText(request.getTone())
                || !StringUtils.hasText(request.getDuration())
                || !StringUtils.hasText(request.getLanguage())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "topic, audience, tone, duration and language are required.");
        }
    }

    private void validateBrief(ContentBrief brief) {
        if (brief == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content brief is required.");
        }
        if (!StringUtils.hasText(brief.getTopic())
                || !StringUtils.hasText(brief.getAudience())
                || !StringUtils.hasText(brief.getTone())
                || !StringUtils.hasText(brief.getDuration())
                || !StringUtils.hasText(brief.getLanguage())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "topic, audience, tone, duration and language are required.");
        }
    }
}
