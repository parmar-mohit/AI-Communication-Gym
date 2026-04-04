package com.thinkschool.coach.communication;

import java.io.FileOutputStream;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.thinkschool.coach.constants.FileLocation;
import com.thinkschool.coach.constants.JsonConstants;
import com.thinkschool.coach.stream.InputEventsProcessor;
import com.thinkschool.coach.stream.OutputEventsProcessor;

import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.http.Protocol;
import software.amazon.awssdk.http.ProtocolNegotiation;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient;
import software.amazon.awssdk.thirdparty.jackson.core.JsonProcessingException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@Component
@Scope("prototype")
public class CoachCallHandler extends TextWebSocketHandler {
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final Logger logger = LoggerFactory.getLogger(CoachCallHandler.class);
	
	private Map<String, SessionManager> sessionManagerMap;
	private Map<String,FileOutputStream> fileOutputStreamMap;
	private BedrockRuntimeAsyncClient client;

	public CoachCallHandler() {
		this.sessionManagerMap = new HashMap<String, SessionManager>();
		this.fileOutputStreamMap = new HashMap<String, FileOutputStream>();
		NettyNioAsyncHttpClient.Builder nettyBuilder = NettyNioAsyncHttpClient.builder()
                .readTimeout(Duration.of(180, ChronoUnit.SECONDS))
                .maxConcurrency(20)
                .protocol(Protocol.HTTP2)
                .protocolNegotiation(ProtocolNegotiation.ALPN);
		
		this.client = BedrockRuntimeAsyncClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .httpClientBuilder(nettyBuilder)
                .build();
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		
		ObjectNode connectionResponseNode = MAPPER.createObjectNode();
		connectionResponseNode.put("status", "connected");
		connectionResponseNode.put("sessionId", session.getId());
		
		session.sendMessage(new TextMessage(connectionResponseNode.toString()));
		
		logger.info("Connected to /coach-call with Session ID : "+session.getId());
		SessionManager sessionManager = new SessionManager(this.client,session.getId());
		sessionManager.setInputStream(new InputEventsProcessor());
		sessionManager.setOutputStream(new OutputEventsProcessor(session));
		sessionManager.createConnection();
		sessionManager.startNewSession();
		
		this.sessionManagerMap.put(session.getId(), sessionManager);
		FileOutputStream videoFile = new FileOutputStream(FileLocation.SESSION_VIDEO_PREFIX+"session-"+session.getId()+".webm", true);
		this.fileOutputStreamMap.put(session.getId(), videoFile);
		logger.info("Connection to Bedrock Created and New Session Started for Session ID : "+session.getId());
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		String rawJsonString = message.getPayload();
		try {
            // Parse the string into a navigable JSON tree
            JsonNode payload = MAPPER.readTree(rawJsonString);
            
            String contentType = payload.get(JsonConstants.CONTENT_TYPE).asString();
            if( contentType.equals(JsonConstants.CONTENT_AUDIO) ) {
            	
            	// DownSample Audio from 48k to 16k and convert to base 64
            	String base64AudioSampleRate16k = payload.get(JsonConstants.CONTENT).asString();
            	this.sessionManagerMap.get(session.getId()).sendUserAudioToBedrock( base64AudioSampleRate16k );
            }else if( contentType.equals(JsonConstants.CONTENT_VIDEO) ) {
            	String base64Video = payload.get(JsonConstants.CONTENT).asString();
            	byte[] videoBytes = Base64.getDecoder().decode(base64Video);
            	FileOutputStream videoFile = this.fileOutputStreamMap.get(session.getId());
            	videoFile.write(videoBytes);
            	videoFile.flush();
            }else if( payload.has(JsonConstants.EVENT) ) {
            	logger.info("Event Received :" + payload);
            	JsonNode event = payload.get(JsonConstants.EVENT);
            	String eventType = event.get(JsonConstants.EVENT_TYPE).asString();
            	
            	switch( eventType ) {
            	case JsonConstants.END_SESSION:
            		String reason = event.get(JsonConstants.REASON).asString();
            		logger.info("Ending Sessiong with Session Id : "+session.getId()+" for reason : "+reason);
            		session.close();
            		break;
            	}
            }
		}catch(JsonProcessingException | NullPointerException e) {
			logger.error("Exception Occured While Converting to Json : "+e.getMessage());
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		super.afterConnectionClosed(session, status);
		this.sessionManagerMap.get(session.getId()).close();
		this.sessionManagerMap.remove(session.getId());
		this.fileOutputStreamMap.get(session.getId()).close();
		this.fileOutputStreamMap.remove(session.getId());
		logger.info("Connection Closed with Session : "+session.getId());
	}
	
}
