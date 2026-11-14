package com.ajit.springAI.VideoGenaration.model;

import jakarta.validation.constraints.NotBlank;

public class RevisionRequest {
    @NotBlank(message = "feedback is required")
    private String feedback;

    @NotBlank(message = "currentScript is required")
    private String currentScript;

    private String version;

    public RevisionRequest() {
    }

    public RevisionRequest(String feedback, String currentScript, String version) {
        this.feedback = feedback;
        this.currentScript = currentScript;
        this.version = version;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getCurrentScript() {
        return currentScript;
    }

    public void setCurrentScript(String currentScript) {
        this.currentScript = currentScript;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
