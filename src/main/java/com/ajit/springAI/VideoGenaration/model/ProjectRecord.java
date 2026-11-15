package com.ajit.springAI.VideoGenaration.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "project_records")
public class ProjectRecord {
    @Id
    private String id;

    @Column(name = "project_name")
    private String projectName;

    @Column(length = 1000)
    private String topic;

    @Column(length = 500)
    private String audience;

    @Column(length = 500)
    private String tone;

    @Column(length = 100)
    private String duration;

    @Column(length = 100)
    private String language;

    @Column(length = 200)
    private String style;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status = ProjectStatus.DRAFT;

    @Column(name = "approved_version")
    private Integer approvedVersion;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "title", column = @Column(name = "current_draft_title")),
            @AttributeOverride(name = "hook", column = @Column(name = "current_draft_hook")),
            @AttributeOverride(name = "narration", column = @Column(name = "current_draft_narration")),
            @AttributeOverride(name = "status", column = @Column(name = "current_draft_status")),
            @AttributeOverride(name = "language", column = @Column(name = "current_draft_language")),
            @AttributeOverride(name = "versionNumber", column = @Column(name = "current_draft_version_number")),
            @AttributeOverride(name = "approvedBy", column = @Column(name = "current_draft_approved_by")),
            @AttributeOverride(name = "approvedAt", column = @Column(name = "current_draft_approved_at"))
    })
    private ScriptDraft currentDraft;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "project_versions", joinColumns = @JoinColumn(name = "project_id"))
    private List<ScriptDraft> versions = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "project_outputs", joinColumns = @JoinColumn(name = "project_id"))
    private List<VideoOutputMetadata> outputs = new ArrayList<>();

    public ProjectRecord() {
    }

    public ProjectRecord(String id, String projectName, String topic, String audience, String tone,
            String duration, String language, String style) {
        this.id = id;
        this.projectName = projectName;
        this.topic = topic;
        this.audience = audience;
        this.tone = tone;
        this.duration = duration;
        this.language = language;
        this.style = style;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public String getTone() {
        return tone;
    }

    public void setTone(String tone) {
        this.tone = tone;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public Integer getApprovedVersion() {
        return approvedVersion;
    }

    public void setApprovedVersion(Integer approvedVersion) {
        this.approvedVersion = approvedVersion;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(Instant approvedAt) {
        this.approvedAt = approvedAt;
    }

    public ScriptDraft getCurrentDraft() {
        return currentDraft;
    }

    public void setCurrentDraft(ScriptDraft currentDraft) {
        this.currentDraft = currentDraft;
    }

    public List<ScriptDraft> getVersions() {
        return versions;
    }

    public void setVersions(List<ScriptDraft> versions) {
        this.versions = versions;
    }

    public List<VideoOutputMetadata> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<VideoOutputMetadata> outputs) {
        this.outputs = outputs;
    }

    public void addGeneratedOutput(VideoOutputMetadata output) {
        if (output != null) {
            this.outputs.add(output);
        }
    }

    public void addVersion(ScriptDraft draft) {
        if (draft != null) {
            if (draft.getVersionNumber() == null) {
                draft.setVersionNumber(this.versions.size() + 1);
            }
            this.versions.add(draft);
            this.currentDraft = draft;
            this.status = ProjectStatus.IN_REVIEW;
            if (draft.getStatus() == null || draft.getStatus().equalsIgnoreCase("generated")
                    || draft.getStatus().equalsIgnoreCase("revised")) {
                draft.setStatus("IN_REVIEW");
            }
        }
    }
}
