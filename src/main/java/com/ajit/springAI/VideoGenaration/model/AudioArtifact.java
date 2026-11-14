package com.ajit.springAI.VideoGenaration.model;

public class AudioArtifact {
    private String audioUrl;

    public AudioArtifact() {
    }

    public AudioArtifact(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }
}
