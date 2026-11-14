package com.ajit.springAI.VideoGenaration.model;

public class PanelPrompt {
    private String scene;
    private String style;

    public PanelPrompt() {
    }

    public PanelPrompt(String scene, String style) {
        this.scene = scene;
        this.style = style;
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }
}
