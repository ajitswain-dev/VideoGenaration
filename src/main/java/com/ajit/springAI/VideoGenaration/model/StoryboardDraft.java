package com.ajit.springAI.VideoGenaration.model;

import java.util.ArrayList;
import java.util.List;

public class StoryboardDraft {
    private String projectId;
    private String version;
    private String title;
    private List<StoryboardPanel> panels = new ArrayList<>();

    public StoryboardDraft() {
    }

    public StoryboardDraft(String projectId, String version, String title, List<StoryboardPanel> panels) {
        this.projectId = projectId;
        this.version = version;
        this.title = title;
        this.panels = panels;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<StoryboardPanel> getPanels() {
        return panels;
    }

    public void setPanels(List<StoryboardPanel> panels) {
        this.panels = panels;
    }
}
