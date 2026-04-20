package com.thinkschool.coach.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thinkschool.coach.dto.ReportGenerationRequest;
import com.thinkschool.coach.dto.ReportGenerationResponse;
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
	
	@Autowired
	public ReportContoller(ReportService reportService, VideoService videoService, S3Service s3Service) {
		this.reportService = reportService;
		this.videoService = videoService;
		this.s3Service = s3Service;
	}

	@PostMapping
    public ReportGenerationResponse getReportJson(@RequestBody ReportGenerationRequest request) {	
		if( !this.videoService.doesVideoExist(request.getSessionId()) && !this.s3Service.doesSessionVideoExist(request.getSessionId()) ) {
			throw new RuntimeException("Some Error Occurred While Generating Report");
		}
        
		reportService.generateAndSendReport(request);
		
        ReportGenerationResponse response = new ReportGenerationResponse("Success");
        return response;
    }
}
