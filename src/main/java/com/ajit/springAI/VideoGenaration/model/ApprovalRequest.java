package com.ajit.springAI.VideoGenaration.model;

public class ApprovalRequest {
    private String projectName;
    private String scriptId;
    private String approvedBy;
    private boolean approved;
    private String comments;

    public ApprovalRequest() {
    }

    public ApprovalRequest(String projectName, String scriptId, String approvedBy, boolean approved, String comments) {
        this.projectName = projectName;
        this.scriptId = scriptId;
        this.approvedBy = approvedBy;
        this.approved = approved;
        this.comments = comments;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getScriptId() {
        return scriptId;
    }

    public void setScriptId(String scriptId) {
        this.scriptId = scriptId;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
