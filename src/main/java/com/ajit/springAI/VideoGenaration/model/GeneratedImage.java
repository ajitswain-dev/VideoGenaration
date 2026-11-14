package com.ajit.springAI.VideoGenaration.model;

public class GeneratedImage {
    private String prompt;
    private String imageUrl;

    public GeneratedImage() {
    }

    public GeneratedImage(String prompt, String imageUrl) {
        this.prompt = prompt;
        this.imageUrl = imageUrl;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
