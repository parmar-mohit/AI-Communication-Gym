package com.thinkschool.coach.controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thinkschool.coach.dto.ReportGenerationRequest;
import com.thinkschool.coach.dto.ReportGenerationResponse;
import com.thinkschool.coach.service.ImageService;
import com.thinkschool.coach.service.MailService;
import com.thinkschool.coach.service.ReportService;
import com.thinkschool.coach.service.S3Service;
import com.thinkschool.coach.service.VideoService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/session-report")
public class ReportContoller {
		
	private ReportService reportService;
	private VideoService videoService;
	private S3Service s3Service;
	private ImageService imageService;
	private MailService mailService;
	
	@Autowired
	public ReportContoller(ReportService reportService, VideoService videoService, S3Service s3Service, ImageService imageService,MailService mailService) {
		this.reportService = reportService;
		this.videoService = videoService;
		this.s3Service = s3Service;
		this.imageService = imageService;
		this.mailService = mailService;
	}

	@PostMapping
    public ReportGenerationResponse getReportJson(@RequestBody ReportGenerationRequest request) {
		String mp4File = this.videoService.convertWebmToMp4(request.getSessionId());
		
		CompletableFuture<Void> fileFuture = CompletableFuture.completedFuture(null);
		
		if( mp4File != null ) {
			List<String> imagePathList = this.imageService.extractImagesFromVideo(mp4File, request.getSessionId());
			
			fileFuture = this.s3Service.uploadSessionVideo(mp4File, request.getSessionId())
			.thenRun( ()->this.s3Service.uploadSessionTranscript(request.getSessionId()) )
			.thenRun( ()-> this.s3Service.uploadSessionImages(imagePathList) )
			.thenRun( ()-> this.videoService.deleteSessionVideoAndTranscript(request.getSessionId()) )
			.thenRun( ()-> this.imageService.deleteImages(imagePathList) );

		}else if( !this.s3Service.doesSessionVideoExist(request.getSessionId()) ) {
			throw new RuntimeException("Some Error Occurred While Generating Report");
		}
		
		CompletableFuture<byte[]> reportPdfFuture = fileFuture.thenCompose( v -> this.reportService.getReport(request) )
		.thenCompose( (analysisReport)-> this.reportService.generatePdf(analysisReport, request.getUserName()) );
		
		
		reportPdfFuture.thenCompose( reportPdf -> this.mailService.sendMail(request.getUserName(), request.getUserEmail(), reportPdf) );
		reportPdfFuture.thenAccept( reportPdf -> this.s3Service.uploadReport(reportPdf, request.getSessionId()) );
        
        ReportGenerationResponse response = new ReportGenerationResponse("Success");
        return response;
    }
}
