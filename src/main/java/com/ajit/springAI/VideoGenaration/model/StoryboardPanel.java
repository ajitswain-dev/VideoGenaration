package com.ajit.springAI.VideoGenaration.model;

public class StoryboardPanel {
    private int panelNumber;
    private int scene;
    private String visualPrompt;
    private String dialogueOrCaption;
    private String duration;
    private String characterReference;

    public StoryboardPanel() {
    }

    public StoryboardPanel(int panelNumber, int scene, String visualPrompt,
            String dialogueOrCaption, String duration, String characterReference) {
        this.panelNumber = panelNumber;
        this.scene = scene;
        this.visualPrompt = visualPrompt;
        this.dialogueOrCaption = dialogueOrCaption;
        this.duration = duration;
        this.characterReference = characterReference;
    }

    public int getPanelNumber() {
        return panelNumber;
    }

    public void setPanelNumber(int panelNumber) {
        this.panelNumber = panelNumber;
    }

    public int getScene() {
        return scene;
    }

    public void setScene(int scene) {
        this.scene = scene;
    }

    public String getVisualPrompt() {
        return visualPrompt;
    }

    public void setVisualPrompt(String visualPrompt) {
        this.visualPrompt = visualPrompt;
    }

    public String getDialogueOrCaption() {
        return dialogueOrCaption;
    }

    public void setDialogueOrCaption(String dialogueOrCaption) {
        this.dialogueOrCaption = dialogueOrCaption;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getCharacterReference() {
        return characterReference;
    }

    public void setCharacterReference(String characterReference) {
        this.characterReference = characterReference;
    }
}
