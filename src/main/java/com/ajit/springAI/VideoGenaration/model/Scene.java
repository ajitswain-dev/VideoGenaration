package com.ajit.springAI.VideoGenaration.model;

public class Scene {
    private int sequence;
    private String visual;
    private String narration;
    private String duration;

    public Scene() {
    }

    public Scene(int sequence, String visual, String narration, String duration) {
        this.sequence = sequence;
        this.visual = visual;
        this.narration = narration;
        this.duration = duration;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getVisual() {
        return visual;
    }

    public void setVisual(String visual) {
        this.visual = visual;
    }

    public String getNarration() {
        return narration;
    }

    public void setNarration(String narration) {
        this.narration = narration;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
