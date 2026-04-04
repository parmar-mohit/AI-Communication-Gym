package com.thinkschool.coach.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thinkschool.coach.dto.ReportResponse;
import com.thinkschool.coach.service.ReportService;
import com.thinkschool.coach.service.S3Service;
import com.thinkschool.coach.service.VideoService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/session-report")
public class ReportContoller {
	
	private static final Logger logger = LoggerFactory.getLogger(ReportContoller.class);
	
	private ReportService reportService;
	private VideoService videoService;
	private S3Service s3Service;
	
	@Autowired
	public ReportContoller(ReportService reportService, VideoService videoService, S3Service s3Service) {
		this.reportService = reportService;
		this.videoService = videoService;
		this.s3Service = s3Service;
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ReportResponse getReportJson(@RequestParam("sessionId") String sessionId) {
		String mp4File = this.videoService.convertWebmToMp4(sessionId);
		
		this.s3Service.uploadSessionVideo(mp4File, sessionId);
		this.s3Service.uploadSessionTranscript(sessionId);
		
		this.videoService.deleteSessionVideo(sessionId);
        return reportService.getReport(sessionId);
    }
	
	@GetMapping(produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getReportPdf(@RequestParam("sessionId") String sessionId) {
		logger.info("Getting PDF Report for Session Id : "+sessionId );

        byte[] pdf = reportService.generatePdf(sessionId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

}
