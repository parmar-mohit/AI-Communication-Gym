package com.thinkschool.coach.service;

import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import com.thinkschool.coach.dto.ReportResponse;

@Service
public class ReportService {
	
	private static final Logger logger = LoggerFactory.getLogger(ReportService.class);
	
	public ReportResponse getReport(String sessionId) {
		logger.info("Getting Report for Session Id : "+sessionId);
		ReportResponse response = new ReportResponse();
		response.setSessionId(sessionId);
		
		//Dummy Data
		response.setStrengths(Arrays.asList("Strength 1","Strength 2","Strength 3"));
		response.setWeakness(Arrays.asList("Weakness 1","Weakness 2","Weakness 3"));
		response.setActionableInsigts(Arrays.asList("Insight 1","Insight 2","Insight 3"));
		
		response.setOverallPerformance("The communication analysis report indicates that the user maintains a generally consistent level of engagement across channels, with peak activity observed during weekday afternoons. Message patterns show a preference for concise and task-oriented interactions, with a moderate response time that suggests balanced availability rather than real-time responsiveness. Sentiment analysis reflects a predominantly neutral to positive tone, with occasional spikes in urgency during high-priority discussions. The user demonstrates strong participation in collaborative threads, frequently initiating follow-ups and clarifications, which contributes to improved overall communication efficiency and alignment within the team.");
		
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

}
