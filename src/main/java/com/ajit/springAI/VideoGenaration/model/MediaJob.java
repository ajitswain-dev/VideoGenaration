package com.ajit.springAI.VideoGenaration.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MediaJob {
    private String id;
    private String projectId;
    private Integer versionNumber;
    private MediaJobStatus status = MediaJobStatus.QUEUED;
    private int progress;
    private String message = "Queued for processing";
    private String errorMessage;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();
    private Instant startedAt;
    private Instant completedAt;
    private boolean cancelled;
    private int attempts;
    private int maxAttempts = 3;
    private long timeoutSeconds = 120;
    private List<String> outputFiles = new ArrayList<>();

    public MediaJob() {
    }

    public MediaJob(String id, String projectId, Integer versionNumber) {
        this.id = id;
        this.projectId = projectId;
        this.versionNumber = versionNumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public MediaJobStatus getStatus() {
        return status;
    }

    public void setStatus(MediaJobStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
        if (status == MediaJobStatus.RUNNING && this.startedAt == null) {
            this.startedAt = Instant.now();
        }
        if ((status == MediaJobStatus.COMPLETED || status == MediaJobStatus.FAILED
                || status == MediaJobStatus.CANCELLED)
                && this.completedAt == null) {
            this.completedAt = Instant.now();
        }
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = Math.max(0, Math.min(100, progress));
        this.updatedAt = Instant.now();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
        this.updatedAt = Instant.now();
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        this.updatedAt = Instant.now();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
        this.updatedAt = Instant.now();
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public long getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public List<String> getOutputFiles() {
        return outputFiles;
    }

    public void setOutputFiles(List<String> outputFiles) {
        this.outputFiles = outputFiles;
    }

    public void addOutputFile(String filePath) {
        if (filePath != null && !filePath.isBlank()) {
            this.outputFiles.add(filePath);
        }
    }

    public void requestCancellation() {
        this.cancelled = true;
        this.message = "Cancellation requested";
        this.updatedAt = Instant.now();
    }
}
