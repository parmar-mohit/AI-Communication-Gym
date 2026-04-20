package com.thinkschool.coach.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.itextpdf.html2pdf.HtmlConverter;
import com.thinkschool.coach.communication.NovaProModel;
import com.thinkschool.coach.dto.ReportGenerationRequest;
import com.thinkschool.coach.model.AnalysisReport;
import com.thinkschool.coach.model.ParsedFrame;
import com.thinkschool.coach.model.ReportMetrics;

@Service
public class ReportService {
	
	private S3Service s3Service;
	private ImageService imageService;
	private NovaProModel novaProModel;
	private VideoService videoService;
	private MailService mailService;
	
	private static final double EYE_CONTACT_THRESHOLD = 10;
	private static final double DISTRACTION_YAW_DELTA = 20.0; // Rapid head turns
	private static final double SWAYING_MOVEMENT_THRESHOLD = 0.05;
	
	@Autowired
	public ReportService(VideoService videoService, S3Service s3Service, ImageService imageService,NovaProModel novaProModel, MailService mailService) {
		this.videoService = videoService;
		this.s3Service = s3Service;
		this.imageService = imageService;
		this.novaProModel = novaProModel;
		this.mailService = mailService;
	}

	private static final Logger logger = LoggerFactory.getLogger(ReportService.class);
	
	@Async
	public void generateAndSendReport(ReportGenerationRequest request) {
		if( this.videoService.doesVideoExist(request.getSessionId()) ) {
			String mp4File = this.videoService.convertWebmToMp4(request.getSessionId());
			List<String> imagePathList = this.imageService.extractImagesFromVideo(mp4File, request.getSessionId());
			
			this.s3Service.uploadSessionImages(imagePathList);
			this.s3Service.uploadSessionVideo(mp4File, request.getSessionId());
			this.s3Service.uploadSessionTranscript(request.getSessionId());
			this.imageService.deleteImages(imagePathList);
			this.videoService.deleteSessionVideo(request.getSessionId());
			this.videoService.deleteSessionTranscript(request.getSessionId());
		}
		
		AnalysisReport analysisReport = this.getReport(request);
		byte[] reportPdf = this.generatePdf(analysisReport, request.getUserName());
		this.mailService.sendMail(request.getUserName(), request.getUserEmail(), reportPdf);
		this.s3Service.uploadReport(reportPdf, request.getSessionId());
	}
	
	private AnalysisReport getReport(ReportGenerationRequest request) {
		logger.info("Getting Report for Session Id : "+request.getSessionId());
		
		List<String> s3ObjectList = this.s3Service.getKeyForSessionImages(request.getSessionId());
		List<ParsedFrame> frames = this.imageService.extractDataFromImages(s3ObjectList);
		
		ReportMetrics reportMetrics = this.processMetrics(frames);
		
		AnalysisReport analysisReport = novaProModel.analyseVideo(request.getSessionId(), reportMetrics);
		analysisReport.setSessionId(request.getSessionId());
		analysisReport.setReportMetrics(reportMetrics);
		
		return analysisReport;
	}
	
	private byte[] generatePdf(AnalysisReport analysisReport,String userName) {
		logger.info("Generating PDF Report for Session Id : "+analysisReport.getSessionId());
		
		// 1. Setup Thymeleaf Resolver
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(resolver);

        // 2. Add Data to Thymeleaf Context
        Context context = new Context();
        context.setVariable("report", analysisReport);
        context.setVariable("userName",userName);

        // 3. Process Template to String
        String htmlContent = templateEngine.process("report_template", context);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // Convert HTML directly into the memory stream
            HtmlConverter.convertToPdf(htmlContent, baos);
            
            // Return the raw bytes 
            return baos.toByteArray();
        } catch (IOException e) {
        	logger.error("Exception Occured : "+e.getMessage(),e);
            throw new RuntimeException("Failed to generate PDF byte array", e);
        }
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
        long smilingFrames = frames.stream().filter(f -> f.isFaceDetected() && f.getIsSmiling()).count();
        return ((double) smilingFrames / frames.size()) * 100.0;
    }
	
	private double calculateFaceTouches(List<ParsedFrame> frames) {
        long touchFrames = frames.stream().filter(f -> f.isFaceDetected() && f.getIsFaceOccluded()).count();
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
