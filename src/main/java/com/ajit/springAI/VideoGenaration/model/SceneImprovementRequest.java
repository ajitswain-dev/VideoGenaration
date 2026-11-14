package com.ajit.springAI.VideoGenaration.model;

public class SceneImprovementRequest {
    private int sceneIndex;
    private String sceneText;
    private String improvementRequest;

    public SceneImprovementRequest() {
    }

    public int getSceneIndex() {
        return sceneIndex;
    }

    public void setSceneIndex(int sceneIndex) {
        this.sceneIndex = sceneIndex;
    }

    public String getSceneText() {
        return sceneText;
    }

    public void setSceneText(String sceneText) {
        this.sceneText = sceneText;
    }

    public String getImprovementRequest() {
        return improvementRequest;
    }

    public void setImprovementRequest(String improvementRequest) {
        this.improvementRequest = improvementRequest;
    }
}
