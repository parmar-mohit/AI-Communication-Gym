package com.thinkschool.coach.model;

import java.util.Map;

public class ReportMetrics {
	private int totalFramesAnalyzed;
    
    // Percentages
    private double eyeContactPercentage;
    private double smilePercentage;
    private double faceTouchPercentage;
    private double distractionPercentage;
    
    // Booleans
    private boolean isConfident;
    private boolean isSwaying;
    
    // Emotion Data
    private String dominantOverallEmotion;
    private Map<String, Double> emotionBreakdown;
    
	public int getTotalFramesAnalyzed() {
		return totalFramesAnalyzed;
	}
	public void setTotalFramesAnalyzed(int totalFramesAnalyzed) {
		this.totalFramesAnalyzed = totalFramesAnalyzed;
	}
	public double getEyeContactPercentage() {
		return eyeContactPercentage;
	}
	public void setEyeContactPercentage(double eyeContactPercentage) {
		this.eyeContactPercentage = eyeContactPercentage;
	}
	public double getSmilePercentage() {
		return smilePercentage;
	}
	public void setSmilePercentage(double smilePercentage) {
		this.smilePercentage = smilePercentage;
	}
	public double getFaceTouchPercentage() {
		return faceTouchPercentage;
	}
	public void setFaceTouchPercentage(double faceTouchPercentage) {
		this.faceTouchPercentage = faceTouchPercentage;
	}
	public double getDistractionPercentage() {
		return distractionPercentage;
	}
	public void setDistractionPercentage(double distractionPercentage) {
		this.distractionPercentage = distractionPercentage;
	}
	public boolean isConfident() {
		return isConfident;
	}
	public void setConfident(boolean isConfident) {
		this.isConfident = isConfident;
	}
	public boolean isSwaying() {
		return isSwaying;
	}
	public void setSwaying(boolean isSwaying) {
		this.isSwaying = isSwaying;
	}
	public String getDominantOverallEmotion() {
		return dominantOverallEmotion;
	}
	public void setDominantOverallEmotion(String dominantOverallEmotion) {
		this.dominantOverallEmotion = dominantOverallEmotion;
	}
	public Map<String, Double> getEmotionBreakdown() {
		return emotionBreakdown;
	}
	public void setEmotionBreakdown(Map<String, Double> emotionBreakdown) {
		this.emotionBreakdown = emotionBreakdown;
	}
	@Override
	public String toString() {
		return "ReportMetrics [totalFramesAnalyzed=" + totalFramesAnalyzed + ", eyeContactPercentage="
				+ eyeContactPercentage + ", smilePercentage=" + smilePercentage + ", faceTouchPercentage="
				+ faceTouchPercentage + ", distractionPercentage=" + distractionPercentage + ", isConfident="
				+ isConfident + ", isSwaying=" + isSwaying + ", dominantOverallEmotion=" + dominantOverallEmotion
				+ ", emotionBreakdown=" + emotionBreakdown + "]";
	}

}
