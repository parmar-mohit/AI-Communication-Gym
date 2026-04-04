package com.thinkschool.coach.stream;

import org.reactivestreams.Publisher;

import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelWithBidirectionalStreamInput;

public interface InputToBedrockStream {
	public Publisher<InvokeModelWithBidirectionalStreamInput> getStream();
	public void sendTextEvent(String promptName,String role, String textData);
	public void sendAudioEvent(String promtpName, String role,String audioData);
	public void sendEvent(String event);
}
