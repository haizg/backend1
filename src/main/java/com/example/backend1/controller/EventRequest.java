package com.example.backend1.controller;

public class EventRequest {
    private String title;
    private String description;
    private String category;
    private String date;
    private String time;
    private String location;
    private String imageUrl;
    private String program;
    private Integer riskScore;
    private String riskReason;
    private String predictedParticipation;
    private String predictedParticipationReason;
    private Integer maxParticipants;

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }
    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }

    public String getRiskReason() { return riskReason; }
    public void setRiskReason(String riskReason) { this.riskReason = riskReason; }

    public String getPredictedParticipation() { return predictedParticipation; }
    public void setPredictedParticipation(String p) { this.predictedParticipation = p; }

    public String getPredictedParticipationReason() { return predictedParticipationReason; }
    public void setPredictedParticipationReason(String r) { this.predictedParticipationReason = r; }
}
