package com.thinkschool.coach.stream;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkschool.coach.constants.Constants;
import com.thinkschool.coach.constants.FileLocation;
import com.thinkschool.coach.constants.PlaceHolders;
import com.thinkschool.coach.utility.FileReader;

import io.reactivex.rxjava3.processors.ReplayProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelWithBidirectionalStreamInput;

public class InputEventsProcessor implements InputToBedrockStream {
	private ReplayProcessor<InvokeModelWithBidirectionalStreamInput> inputStream;
	
	private static final Logger bedrocokInputLogger = LoggerFactory.getLogger(Constants.BEDROCK_INPUT_LOGGER);
	
	public InputEventsProcessor() {
		this.inputStream = ReplayProcessor.createWithTime(
                1, TimeUnit.MINUTES, Schedulers.io()
        );
	}
	
	@Override
	public Publisher<InvokeModelWithBidirectionalStreamInput> getStream() {
		return this.inputStream;
	}


	@Override
	public void sendTextEvent(String promptName, String role, String textData) {
		String contentName = UUID.randomUUID().toString();
		
		// Send Content Start Event
		String textContentStartEvent = FileReader.getMessage(FileLocation.TEXT_CONTENT_START_EVENT);
		textContentStartEvent = textContentStartEvent.replace(PlaceHolders.PROMPT_NAME,promptName);
		textContentStartEvent = textContentStartEvent.replace(PlaceHolders.CONTENT_NAME, contentName);
		textContentStartEvent = textContentStartEvent.replace(PlaceHolders.ROLE, role);
		this.sendEvent(textContentStartEvent);
		
		// Send Content
		String textContentInput = FileReader.getMessage(FileLocation.TEXT_INPUT_CONTENT);
		textContentInput = textContentInput.replace(PlaceHolders.PROMPT_NAME, promptName);
		textContentInput = textContentInput.replace(PlaceHolders.CONTENT_NAME, contentName);
		textContentInput = textContentInput.replace(PlaceHolders.TEXT, textData);
		this.sendEvent(textContentInput);
		
		// Send Content End Event
		this.sendContentEndEvent(promptName, contentName);
	}

	@Override
	public void sendAudioEvent(String promptName, String role, String audioData) {
		String contentName = UUID.randomUUID().toString();
		
		// Send Content Start Event
		String audioContentStartEvent = FileReader.getMessage(FileLocation.AUDIO_CONTENT_START_EVENT);
		audioContentStartEvent = audioContentStartEvent.replace(PlaceHolders.PROMPT_NAME, promptName);
		audioContentStartEvent = audioContentStartEvent.replace(PlaceHolders.CONTENT_NAME, contentName);
		audioContentStartEvent = audioContentStartEvent.replace(PlaceHolders.ROLE, role);
		this.sendEvent(audioContentStartEvent);
		
		// Send Content
		String audioContentInput = FileReader.getMessage(FileLocation.AUDIO_INPUT_CONTENT);
		audioContentInput = audioContentInput.replace(PlaceHolders.PROMPT_NAME, promptName);
		audioContentInput = audioContentInput.replace(PlaceHolders.CONTENT_NAME, contentName);
		audioContentInput = audioContentInput.replace(PlaceHolders.AUDIO, audioData);
		this.sendEvent(audioContentInput);
		
		// Send Content End Event
		this.sendContentEndEvent(promptName, contentName);
	}
	
	private void sendContentEndEvent(String promptName,String contentName) {
		String contentEndEvent = FileReader.getMessage(FileLocation.CONTENT_END_EVENT);
		contentEndEvent = contentEndEvent.replace(PlaceHolders.PROMPT_NAME, promptName);
		contentEndEvent = contentEndEvent.replace(PlaceHolders.CONTENT_NAME, contentName);
		this.sendEvent(contentEndEvent);
	}
	
	public void sendEvent(String event) {
		bedrocokInputLogger.info("Sending Event to Bedrock : "+event);
        this.inputStream.onNext(
                InvokeModelWithBidirectionalStreamInput.chunkBuilder()
                        .bytes(SdkBytes.fromUtf8String(event))
                        .build()
        );
	}
}
