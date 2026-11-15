package com.ajit.springAI.VideoGenaration.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.Instant;

@Embeddable
public class VideoOutputMetadata {
    @Column(name = "output_project_id")
    private String projectId;
    private Integer approvedScriptVersion;
    private String provider;
    private String model;
    private String outputPath;
    private String metadataPath;

    @Enumerated(EnumType.STRING)
    private GenerationStatus status = GenerationStatus.QUEUED;

    private Instant createdAt = Instant.now();

    public VideoOutputMetadata() {
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public Integer getApprovedScriptVersion() {
        return approvedScriptVersion;
    }

    public void setApprovedScriptVersion(Integer approvedScriptVersion) {
        this.approvedScriptVersion = approvedScriptVersion;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getMetadataPath() {
        return metadataPath;
    }

    public void setMetadataPath(String metadataPath) {
        this.metadataPath = metadataPath;
    }

    public GenerationStatus getStatus() {
        return status;
    }

    public void setStatus(GenerationStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
