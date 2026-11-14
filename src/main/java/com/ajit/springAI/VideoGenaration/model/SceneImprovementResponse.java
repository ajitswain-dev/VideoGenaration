package com.ajit.springAI.VideoGenaration.model;

public class SceneImprovementResponse {
    private int sceneIndex;
    private String improvedText;

    public SceneImprovementResponse() {
    }

    public int getSceneIndex() {
        return sceneIndex;
    }

    public void setSceneIndex(int sceneIndex) {
        this.sceneIndex = sceneIndex;
    }

    public String getImprovedText() {
        return improvedText;
    }

    public void setImprovedText(String improvedText) {
        this.improvedText = improvedText;
    }
}
