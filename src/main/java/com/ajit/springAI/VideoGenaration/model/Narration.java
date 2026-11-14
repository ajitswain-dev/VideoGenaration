package com.ajit.springAI.VideoGenaration.model;

public class Narration {
    private String text;

    public Narration() {
    }

    public Narration(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
