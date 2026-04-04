package com.thinkschool.coach.stream;

public interface OutputFromBedrockStream {
	public void onEventReceived(String payloadString);
	public void close();
}
