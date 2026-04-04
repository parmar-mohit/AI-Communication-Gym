package com.thinkschool.coach.stream;

import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.thinkschool.coach.constants.FileLocation;
import com.thinkschool.coach.constants.JsonConstants;
import com.thinkschool.coach.utility.ContentType;

import software.amazon.awssdk.thirdparty.jackson.core.JsonProcessingException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

public class OutputEventsProcessor implements OutputFromBedrockStream {
	
	private static final Logger logger = LoggerFactory.getLogger(OutputEventsProcessor.class);
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	private ContentType nextContentType;
	private String nextContentRole;
	
	private WebSocketSession session;
	private FileWriter transcriptFile;
	
	
	
	public OutputEventsProcessor(WebSocketSession session) {
		this.session = session;
		try {
			this.transcriptFile = new FileWriter(FileLocation.SESSION_TRANSCRIPT_PREFIX+"session-"+session.getId()+".txt",true);
		} catch (IOException e) {
			logger.error("Exception Occurred : "+e.getMessage());
			logger.error("Exception Trace : ",e);
		}
	}
	
	@Override
	public void onEventReceived(String payloadString) {
		JsonNode payload;
		try {
			payload = MAPPER.readTree(payloadString);
		
		
			if( payload.has(JsonConstants.EVENT) ) {
				JsonNode event = payload.get(JsonConstants.EVENT);
				
				// Start Event
				if( event.has(JsonConstants.CONTENT_START_EVENT) ) {
				
					nextContentType = ContentType.fromValue(event.get(JsonConstants.CONTENT_START_EVENT).get(JsonConstants.CONTENT_TYPE_BEDROCK).asString());
					if( nextContentType == ContentType.TEXT ) {
						String generationStage = event.get(JsonConstants.CONTENT_START_EVENT).get(JsonConstants.ADDITIONAL_MODEL_FIELDS).asString();
						if( !generationStage.equals(JsonConstants.GENERATION_STAGE_FINAL) ) { // Skip Processing if generation stage is speculative
							nextContentType = null;
							return;
						}
					}
					nextContentRole = event.get(JsonConstants.CONTENT_START_EVENT).get(JsonConstants.ROLE).asString();
				}else if( event.has(JsonConstants.CONTENT_END_EVENT) ) { // Content End Event
					this.nextContentRole = null;
					this.nextContentType = null;
				}else {
					// Text Transription Output
					if( nextContentType == ContentType.TEXT && event.has(JsonConstants.TEXT_OUTPUT) ) {
						String transcribedText = event.get(JsonConstants.TEXT_OUTPUT).get(JsonConstants.CONTENT).asString();
						
						if( transcribedText.equals( "{ \"interrupted\" : true }" ) ){ // Do not send interrupt status
							return;
						}
						
						// Send Transcript to Client
						ObjectNode response = MAPPER.createObjectNode();
						ObjectNode transcriptNode = MAPPER.createObjectNode();
						transcriptNode.put("role",nextContentRole);
						transcriptNode.put("text",transcribedText);
						response.set("transcript",transcriptNode);
						
						session.sendMessage(new TextMessage(response.toString()));
						transcriptFile.write(nextContentRole + " : "+transcribedText+"\n");
					}else if( nextContentType == ContentType.AUDIO && event.has(JsonConstants.AUDIO_OUTPUT) ) { // Audio Output
						String base64Audio = event.get(JsonConstants.AUDIO_OUTPUT).get(JsonConstants.CONTENT).asString();
						
						ObjectNode response = MAPPER.createObjectNode();
						ObjectNode audioOutputNode = MAPPER.createObjectNode();
						audioOutputNode.put("audio", base64Audio);
						response.set(JsonConstants.AUDIO_OUTPUT, audioOutputNode);
						
						session.sendMessage(new TextMessage(response.toString()));
					}
				}
			}
		} catch (JsonProcessingException | NullPointerException e) {
			logger.error("Exception Occured While Converting to Json : "+e.getMessage());
			logger.error("Exception Trace : ", e);
		} catch (IOException e) {
			logger.error("Exception Occured while sending data to client : "+e.getMessage());
			logger.error("Excpetion Trace : ",e);
		}
	}
	
	public void close() {
		try {
			this.transcriptFile.close();
		} catch (IOException e) {
			logger.error("Exception Occurred : "+e.getMessage());
			logger.error("Exception Trace : ",e);
		}
	}
}
