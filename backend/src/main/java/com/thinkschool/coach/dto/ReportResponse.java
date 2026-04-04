package com.thinkschool.coach.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReportResponse {
	
	private String sessionId;
	private List<String> strengths;
	private List<String> weakness;
	
	@JsonProperty("actionable-insights")
	private List<String> actionableInsigts;
	
	@JsonProperty("overall-performance")
	private String overallPerformance;

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public List<String> getStrengths() {
		return strengths;
	}

	public void setStrengths(List<String> strengths) {
		this.strengths = strengths;
	}

	public List<String> getWeakness() {
		return weakness;
	}

	public void setWeakness(List<String> weakness) {
		this.weakness = weakness;
	}

	public List<String> getActionableInsigts() {
		return actionableInsigts;
	}

	public void setActionableInsigts(List<String> actionableInsigts) {
		this.actionableInsigts = actionableInsigts;
	}

	public String getOverallPerformance() {
		return overallPerformance;
	}

	public void setOverallPerformance(String overallPerformance) {
		this.overallPerformance = overallPerformance;
	}

}
