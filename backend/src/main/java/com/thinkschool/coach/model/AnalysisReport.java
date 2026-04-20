package com.thinkschool.coach.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AnalysisReport {
	
	private String sessionId;
	private List<String> strengths;
	private List<String> weakness;
	
	@JsonProperty("actionable-insights")
	private List<String> actionableInsights;
	
	@JsonProperty("overall-performance")
	private String overallPerformance;
	private ReportMetrics reportMetrics;
	private int score; // Score out of 100
	private String articulationAndClarity; // Does the user mumble, or is every word distinct?
	private String pacing; // (Words Per Minute): Is the speed appropriate for the context, or do they rush when nervous?
	private String fillerWordAnalysis; // Frequency of "um," "uh," "like," or "you know."
	private String toneAndInflection; // Is the voice monotonic, or does it carry appropriate energy and emphasis?
	private String conciseness; // Do they get to the point, or do they ramble?
	private String vocabularyRange; // Use of precise language versus repetitive or vague terms.
}
