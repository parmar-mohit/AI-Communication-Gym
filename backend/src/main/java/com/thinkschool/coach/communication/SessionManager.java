package com.thinkschool.coach.communication;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkschool.coach.constants.Constants;
import com.thinkschool.coach.constants.FileLocation;
import com.thinkschool.coach.constants.Role;
import com.thinkschool.coach.stream.InputToBedrockStream;
import com.thinkschool.coach.stream.OutputFromBedrockStream;
import com.thinkschool.coach.utility.FileReader;

import software.amazon.awssdk.core.async.SdkPublisher;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockruntime.model.BidirectionalOutputPayloadPart;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelWithBidirectionalStreamOutput;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelWithBidirectionalStreamRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelWithBidirectionalStreamResponse;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelWithBidirectionalStreamResponseHandler;

public class SessionManager implements InvokeModelWithBidirectionalStreamResponseHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
	private static final Logger bedrockOutputLogger = LoggerFactory.getLogger(Constants.BEDROCK_OUTPUT_LOGGER);
	
	private BedrockRuntimeAsyncClient bedrockClient;
	private String promptName;
	private InputToBedrockStream inputStream;
	private OutputFromBedrockStream outputStream;
	private String sessionId;
	
	public SessionManager(BedrockRuntimeAsyncClient client,String sessionId) {
		this.bedrockClient = client;
		this.promptName = UUID.randomUUID().toString();
		this.sessionId = sessionId;
	}
	
	
	public InputToBedrockStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputToBedrockStream inputStream) {
		this.inputStream = inputStream;
	}

	public OutputFromBedrockStream getOutputStream() {
		return outputStream;
	}

	public void setOutputStream(OutputFromBedrockStream outputStream) {
		this.outputStream = outputStream;
	}

	public void createConnection() {
		InvokeModelWithBidirectionalStreamRequest request = InvokeModelWithBidirectionalStreamRequest.builder()
                .modelId(Constants.SONIC_SPEECH_MODEL_ID)
                .build();

        CompletableFuture<Void> completableFuture = bedrockClient.invokeModelWithBidirectionalStream(request, this.inputStream.getStream(), this);

        // if the request fails make sure to tell the publisher to close down properly
        completableFuture.exceptionally(throwable -> {
        	logger.error("Exception Occured While Receiving message on stream from bedrock "+throwable.getMessage());
        	logger.error("Excption Trace : ",throwable);
            return null;
        });

        // if the request finishes make sure to close the publisher properly
        completableFuture.thenApply(result -> {
            // TODO: Add Log and Exception Handling
            return result;
        });
	}
	
	public void startNewSession() {
		String sessionStartEvent = FileReader.getMessage(FileLocation.SESSION_START_EVENT);
		this.inputStream.sendEvent(sessionStartEvent);
		
		String promptStartEvent = FileReader.getMessage(FileLocation.PROMPT_START_EVENT);
		promptStartEvent = promptStartEvent.replace("${promptName}", this.promptName);
		this.inputStream.sendEvent(promptStartEvent);
		
		
		String systemPrompt = FileReader.getMessage(FileLocation.SYSTEM_PROMPT);
		systemPrompt = systemPrompt.substring(0,systemPrompt.length()-2);
		this.inputStream.sendTextEvent(this.promptName, "SYSTEM", systemPrompt);
		
		// Sending Initial Audio Mannual to Force Model to Start Conversation
		try {
            // Load file from resources
            InputStream inputStream = SessionManager.class
                    .getClassLoader()
                    .getResourceAsStream(FileLocation.INTIAL_SESSION_AUDIO);

            if (inputStream == null) {
                throw new RuntimeException("File not found in resources");
            }

            // Read all bytes
            byte[] audioBytes = inputStream.readAllBytes();

            // Convert to Base64
            String base64Encoded = Base64.getEncoder().encodeToString(audioBytes);

        	this.inputStream.sendAudioEvent(promptName, "USER", base64Encoded);
        	logger.info("Initital Session Audio Sent");
        } catch (Exception e) {
        	logger.error("Exception Occurred : "+e.getMessage());
        	logger.error("Exception Trace : ",e);
        }
	}
	
	public void sendUserAudioToBedrock(String base64Audio) {
		this.inputStream.sendAudioEvent(this.promptName, Role.USER, base64Audio);
	}
	
	
	public void endSession() {
		String promptEndEvent = FileReader.getMessage(FileLocation.PROMPT_END_EVENT);
		promptEndEvent = promptEndEvent.replace("${promptName}", this.promptName);
		this.inputStream.sendEvent(promptEndEvent);
		
		String sessionEndEvent = FileReader.getMessage(FileLocation.SESSION_END_EVENT);
		this.inputStream.sendEvent(sessionEndEvent);
	}

	@Override
	public void responseReceived(InvokeModelWithBidirectionalStreamResponse response) {
		bedrockOutputLogger.info("Amazon Nova Sonic request id: "+response.responseMetadata());
	}

	@Override
	public void onEventStream(SdkPublisher<InvokeModelWithBidirectionalStreamOutput> publisher) {
		CompletableFuture<Void> completableFuture = publisher.subscribe(
			(output) -> output.accept(
				new Visitor() {
					@Override
					public void visitChunk(BidirectionalOutputPayloadPart event) {
						String payloadString = StandardCharsets.UTF_8.decode( event.bytes().asByteBuffer().rewind().duplicate() ).toString();
						bedrockOutputLogger.info("Data From Bedrock : "+payloadString);
						outputStream.onEventReceived(payloadString);
					}
				}
			)
		);

        completableFuture.exceptionally(throwable -> {
        	bedrockOutputLogger.error("Exception Occured While Receiving message on stream from bedrock "+throwable.getMessage());
        	bedrockOutputLogger.error("Excption Trace : ",throwable);
        	return null;
        });
	}

	@Override
	public void exceptionOccurred(Throwable throwable) {
		logger.error("Exception Occured While Receiving message on stream from bedrock "+throwable.getMessage());
		logger.error("Excption Trace : ",throwable);	
	}

	@Override
	public void complete() {
		logger.info("Bedrock Connection Completed with Session : "+sessionId);
	}
	
	public void close() {
		this.endSession();
		this.outputStream.close();
	}
}
