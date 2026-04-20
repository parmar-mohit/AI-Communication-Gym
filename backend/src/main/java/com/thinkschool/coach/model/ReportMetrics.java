package com.thinkschool.coach.model;

import java.util.Map;

import lombok.Data;

@Data
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
}
