package com.ajit.springAI.VideoGenaration.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Embeddable
public class ScriptDraft {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private String title;
    private String hook;
    private String narration;

    @Lob
    @Column(name = "scenes_json", columnDefinition = "TEXT")
    private String scenesJson;

    private String status;
    private String language;
    private Integer versionNumber;
    private String approvedBy;
    private Instant approvedAt;

    public ScriptDraft() {
    }

    public ScriptDraft(String title, String hook, String narration, List<Scene> scenes, String status,
            String language) {
        this.title = title;
        this.hook = hook;
        this.narration = narration;
        this.setScenes(scenes);
        this.status = status;
        this.language = language;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHook() {
        return hook;
    }

    public void setHook(String hook) {
        this.hook = hook;
    }

    public String getNarration() {
        return narration;
    }

    public void setNarration(String narration) {
        this.narration = narration;
    }

    public List<Scene> getScenes() {
        if (scenesJson == null || scenesJson.isBlank()) {
            return new ArrayList<>();
        }

        try {
            return OBJECT_MAPPER.readValue(scenesJson, new TypeReference<List<Scene>>() {
            });
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }

    public void setScenes(List<Scene> scenes) {
        try {
            this.scenesJson = (scenes == null || scenes.isEmpty()) ? "[]" : OBJECT_MAPPER.writeValueAsString(scenes);
        } catch (JsonProcessingException e) {
            this.scenesJson = "[]";
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
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
}
