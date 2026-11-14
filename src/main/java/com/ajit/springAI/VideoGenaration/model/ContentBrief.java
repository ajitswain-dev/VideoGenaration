package com.ajit.springAI.VideoGenaration.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public class ContentBrief {
    @NotBlank(message = "topic is required")
    private String topic;

    @NotBlank(message = "audience is required")
    private String audience;

    @NotBlank(message = "tone is required")
    private String tone;

    @NotBlank(message = "duration is required")
    private String duration;

    @NotBlank(message = "language is required")
    private String language;

    private String style;

    public ContentBrief() {
    }

    public ContentBrief(String topic, String audience, String tone, String duration, String language, String style) {
        this.topic = topic;
        this.audience = audience;
        this.tone = tone;
        this.duration = duration;
        this.language = language;
        this.style = style;
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

    @JsonAlias({ "targetDuration" })
    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getTargetDuration() {
        return duration;
    }

    public void setTargetDuration(String targetDuration) {
        this.duration = targetDuration;
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
}
