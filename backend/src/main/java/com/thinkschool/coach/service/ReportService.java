package com.thinkschool.coach.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import com.thinkschool.coach.dto.ReportResponse;
import com.thinkschool.coach.model.ParsedFrame;
import com.thinkschool.coach.model.ReportMetrics;

@Service
public class ReportService {
	
	private S3Service s3Service;
	private ImageService imageService;
	
	private static final double EYE_CONTACT_THRESHOLD = 0.15;
	private static final double DISTRACTION_YAW_DELTA = 20.0; // Rapid head turns
	private static final double SWAYING_MOVEMENT_THRESHOLD = 0.05;
	
	@Autowired
	public ReportService(S3Service s3Service, ImageService imageService) {
		this.s3Service = s3Service;
		this.imageService = imageService;
	}

	private static final Logger logger = LoggerFactory.getLogger(ReportService.class);
	
	public ReportResponse getReport(String sessionId) {
		logger.info("Getting Report for Session Id : "+sessionId);
		
		List<String> s3ObjectList = this.s3Service.getKeyForSessionImages(sessionId);
		List<ParsedFrame> frames = this.imageService.extractDataFromImages(s3ObjectList);
		
		ReportMetrics reportMetrics = this.processMetrics(frames);
		
		ReportResponse response = new ReportResponse();
		response.setSessionId(sessionId);
		
		//Dummy Data
		response.setStrengths(Arrays.asList("Strength 1","Strength 2","Strength 3"));
		response.setWeakness(Arrays.asList("Weakness 1","Weakness 2","Weakness 3"));
		response.setActionableInsigts(Arrays.asList("Insight 1","Insight 2","Insight 3"));
		
		response.setOverallPerformance("The communication analysis report indicates that the user maintains a generally consistent level of engagement across channels, with peak activity observed during weekday afternoons. Message patterns show a preference for concise and task-oriented interactions, with a moderate response time that suggests balanced availability rather than real-time responsiveness. Sentiment analysis reflects a predominantly neutral to positive tone, with occasional spikes in urgency during high-priority discussions. The user demonstrates strong participation in collaborative threads, frequently initiating follow-ups and clarifications, which contributes to improved overall communication efficiency and alignment within the team.");
		
		response.setReportMetrics(reportMetrics);
		return response;
	}
	
	public byte[] generatePdf(String sessionId) {
		logger.info("Generating PDF Report for Session Id : "+sessionId);
		try {
			ClassPathResource resource = new ClassPathResource("report.pdf");
	        return StreamUtils.copyToByteArray(resource.getInputStream());
		} catch (IOException e) {
			logger.error("Exception Occurred : "+e.getMessage());
			logger.error("Exception Trace : ",e);
		}
		return null;
	}
	
	private ReportMetrics processMetrics(List<ParsedFrame> frames) {
		ReportMetrics reportMetrics = new ReportMetrics();
		
		reportMetrics.setTotalFramesAnalyzed(frames.size());
		reportMetrics.setEyeContactPercentage(this.calculateEyeContactPercentage(frames));
		reportMetrics.setSmilePercentage(this.calculateSmilePercentage(frames));
		reportMetrics.setFaceTouchPercentage(this.calculateFaceTouches(frames));
		reportMetrics.setDistractionPercentage(this.calculateDistraction(frames));
		reportMetrics.setSwaying(this.detectSwaying(frames));
		reportMetrics.setEmotionBreakdown(this.calculateEmotionBreakdown(frames));
		reportMetrics.setConfident(this.determineConfidence(reportMetrics.getEmotionBreakdown(), reportMetrics.getSmilePercentage()));
		reportMetrics.setDominantOverallEmotion(this.getDominantEmotion(reportMetrics.getEmotionBreakdown()));
		
		logger.info("Logger Metrics Are as Follows : "+reportMetrics);
		return reportMetrics;
	}
	
	private String getDominantEmotion(Map<String,Double> emotionBreakdown) {
		String dominantEmotion = "UNKNOWN";
		Double maxVal = 0.0;
		for( Map.Entry<String, Double> entry: emotionBreakdown.entrySet() ) {
			if( entry.getValue() > maxVal ) {
				dominantEmotion = entry.getKey();
				maxVal = entry.getValue();
			}
		}
		
		return dominantEmotion;
	}
	
	private double calculateEyeContactPercentage(List<ParsedFrame> frames) {
		// We calculate where the eyes are looking relative to the physical webcam
        long goodEyeContactFrames = frames.stream()
        		.filter(f->f.isFaceDetected())
                .filter(f -> {
                    // Combine head position with eyeball position
                    double absoluteGazeYaw = f.getHeadYaw() + f.getPupilYaw();
                    double absoluteGazePitch = f.getHeadPitch() + f.getPupilPitch();
                    
                    // The combined gaze must be pointed at the lens
                    return Math.abs(absoluteGazeYaw) <= EYE_CONTACT_THRESHOLD && 
                           Math.abs(absoluteGazePitch) <= EYE_CONTACT_THRESHOLD;
                })
                .count();
                
        return ((double) goodEyeContactFrames / frames.size()) * 100.0;
	}
	
	private double calculateSmilePercentage(List<ParsedFrame> frames) {
        long smilingFrames = frames.stream().filter(f -> f.isFaceDetected() && f.isSmiling()).count();
        return ((double) smilingFrames / frames.size()) * 100.0;
    }
	
	private double calculateFaceTouches(List<ParsedFrame> frames) {
        long touchFrames = frames.stream().filter(f -> f.isFaceDetected() && f.isFaceOccluded()).count();
        return ((double) touchFrames / frames.size()) * 100.0;
    }
	
	private double calculateDistraction(List<ParsedFrame> frames) {
        int distractedMoments = 0;
        
        frames = frames.stream().filter(f->f.isFaceDetected()).collect(Collectors.toList());
        
        for (int i = 1; i < frames.size(); i++) {
        	
        	// Pitch is not used to avoid false positive, as humans also nod while having a converstaion
            double previousHeadYaw = frames.get(i - 1).getHeadYaw();
            double currentHeadYaw = frames.get(i).getHeadYaw();
            
            // If the head sharply turns more than the delta threshold between frames
            if (Math.abs(currentHeadYaw - previousHeadYaw) > DISTRACTION_YAW_DELTA) {
                distractedMoments++;
            }
        }
        return ((double) distractedMoments / frames.size()) * 100.0;
    }
	
	private boolean detectSwaying(List<ParsedFrame> frames) {
        double totalMovement = 0.0;
        
        frames = frames.stream().filter(f->f.isFaceDetected()).collect(Collectors.toList());
        
        for (int i = 1; i < frames.size(); i++) {
            double prevCenter = frames.get(i - 1).getFaceCenterX();
            double currentCenter = frames.get(i).getFaceCenterX();
            totalMovement += Math.abs(currentCenter - prevCenter);
        }
        
        double averageMovementPerFrame = totalMovement / frames.size();
        return averageMovementPerFrame > SWAYING_MOVEMENT_THRESHOLD;
	}
    
    private Map<String,Double> calculateEmotionBreakdown(List<ParsedFrame> frames){
    	frames = frames.stream().filter(f->f.isFaceDetected()).collect(Collectors.toList());
    	
		Map<String, Integer> counts = new HashMap<>();
        for (ParsedFrame f : frames) {
            counts.put(f.getDominantEmotion(), counts.getOrDefault(f.getDominantEmotion(), 0) + 1);
        }

        Map<String, Double> breakdown = new HashMap<>();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            breakdown.put(entry.getKey(), ((double) entry.getValue() / frames.size()) * 100.0);
        }
        return breakdown;
	}
    
    /**
     * Confidence is true if Positive emotions + Smiles outweigh Negative emotions.
     */
    private boolean determineConfidence(Map<String, Double> emotions, double smilePercentage) {
        double positiveScore = emotions.getOrDefault("CALM", 0.0) + 
                               emotions.getOrDefault("HAPPY", 0.0) + 
                               (smilePercentage * 0.5); // Give a slight weight boost for smiling
                               
        double negativeScore = emotions.getOrDefault("FEAR", 0.0) + 
                               emotions.getOrDefault("CONFUSED", 0.0) + 
                               emotions.getOrDefault("SAD", 0.0) + 
                               emotions.getOrDefault("ANGRY", 0.0);

        return positiveScore > negativeScore;
    }

}
