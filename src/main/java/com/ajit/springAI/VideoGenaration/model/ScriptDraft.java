package com.ajit.springAI.VideoGenaration.model;

import java.util.ArrayList;
import java.util.List;

public class ScriptDraft {
    private String title;
    private String hook;
    private String narration;
    private List<Scene> scenes = new ArrayList<>();
    private String status;
    private String language;

    public ScriptDraft() {
    }

    public ScriptDraft(String title, String hook, String narration, List<Scene> scenes, String status,
            String language) {
        this.title = title;
        this.hook = hook;
        this.narration = narration;
        this.scenes = scenes;
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
        return scenes;
    }

    public void setScenes(List<Scene> scenes) {
        this.scenes = scenes;
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
}
