package com.ajit.springAI.VideoGenaration.model;

import java.util.ArrayList;
import java.util.List;

public class ProjectRecord {
    private String id;
    private String projectName;
    private String topic;
    private String audience;
    private String tone;
    private String duration;
    private String language;
    private String style;
    private ScriptDraft currentDraft;
    private List<ScriptDraft> versions = new ArrayList<>();

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

    public void addVersion(ScriptDraft draft) {
        if (draft != null) {
            this.versions.add(draft);
            this.currentDraft = draft;
        }
    }
}
