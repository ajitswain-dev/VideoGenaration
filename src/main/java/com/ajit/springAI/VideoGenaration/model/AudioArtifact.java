package com.ajit.springAI.VideoGenaration.model;

public class AudioArtifact {
    private String audioUrl;
    private String captionUrl;
    private String provider;
    private String model;
    private String status;

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

    public String getCaptionUrl() {
        return captionUrl;
    }

    public void setCaptionUrl(String captionUrl) {
        this.captionUrl = captionUrl;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
