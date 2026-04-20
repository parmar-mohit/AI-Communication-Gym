package com.thinkschool.coach.dto;

import lombok.Data;

@Data
public class ReportGenerationRequest {
	private String sessionId;
	private String userName;
	private String userEmail;
}
