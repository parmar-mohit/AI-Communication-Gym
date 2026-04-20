package com.thinkschool.coach.communication;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.thinkschool.coach.constants.Constants;
import com.thinkschool.coach.constants.FileLocation;
import com.thinkschool.coach.model.ReportMetrics;
import com.thinkschool.coach.model.AnalysisReport;
import com.thinkschool.coach.service.S3Service;
import com.thinkschool.coach.utility.FileReader;

import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.ContentBlock;
import software.amazon.awssdk.services.bedrockruntime.model.ConversationRole;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseRequest;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseResponse;
import software.amazon.awssdk.services.bedrockruntime.model.DocumentBlock;
import software.amazon.awssdk.services.bedrockruntime.model.DocumentSource;
import software.amazon.awssdk.services.bedrockruntime.model.Message;
import software.amazon.awssdk.services.bedrockruntime.model.VideoBlock;
import software.amazon.awssdk.services.bedrockruntime.model.VideoSource;
import software.amazon.awssdk.thirdparty.jackson.core.JsonProcessingException;
import tools.jackson.databind.ObjectMapper;

@Component
public class NovaProModel {
	
	private BedrockRuntimeClient client;
	private ObjectMapper objectMapper;
	
	private static final Logger logger = LoggerFactory.getLogger(NovaProModel.class);
	
	public NovaProModel() {
		this.client = BedrockRuntimeClient.builder()
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .region(Region.US_EAST_1)
             // 1. Tell the underlying HTTP client to wait up to 5 minutes for a response
                .httpClientBuilder(ApacheHttpClient.builder()
                    .socketTimeout(Duration.ofMinutes(5))
                    .connectionTimeout(Duration.ofSeconds(10)))
                    
                // 2. Tell the AWS SDK overall to allow the attempt to last 5 minutes
                .overrideConfiguration(config -> config
                    .apiCallTimeout(Duration.ofMinutes(5))
                    .apiCallAttemptTimeout(Duration.ofMinutes(5)))
                .build();
		this.objectMapper = new ObjectMapper();
	}
	
	private AnalysisReport converse(ConverseRequest request) throws JsonProcessingException {
		ConverseResponse response = client.converse(request);
		
		// Extract response text
		String output = response.output()
		    .message()
		    .content()
		    .get(0)
		    .text();
		logger.info("Message Received from Nova Pro Model : "+output);
		
		return objectMapper.readValue(output, AnalysisReport.class);
	}
	
	public AnalysisReport analyseVideo(String sessionId,ReportMetrics reportMetrics) {
		Message message = Message.builder()
				.role(ConversationRole.USER)
				.content(
					List.of(
							
						//Video From S3
						ContentBlock.builder()
						.video(VideoBlock.builder()
								.format("mp4")
								.source(VideoSource.builder()
										.s3Location(s3->s3.uri("s3://"+S3Service.SESSION_VIDEO_BUCKET+"/"+sessionId))
										.build())
								.build())
						.build(),
						
						// Document From s3
						ContentBlock.builder()
						.document(DocumentBlock.builder()
								.name("transcript")
								.format("txt")
								.source(DocumentSource.builder()
										.s3Location(s3->s3.uri("s3://"+S3Service.SESSION_TRANSCRIPT_BUCKET+"/"+sessionId))
										.build())
								.build())
						.build(),
						
						//Metrics Block
						ContentBlock.builder()
						.text(this.objectMapper.writeValueAsString(reportMetrics))
						.build(),
						
						
						// Instruction Prompt
						ContentBlock.builder()
						.text(FileReader.getMessage(FileLocation.INSTRUCTION_PROMPT))
						.build()
					)
				).build();
		
		ConverseRequest request = ConverseRequest.builder()
				.modelId(Constants.NOVA_TEXT_MODEL_ID)
				.messages(message)
				.build();
		
		AnalysisReport response = null;
		
		for( int i = 0; i < 5; i++ ) {
			try {
				response = converse(request);
				break;
			}catch(JsonProcessingException e) {
				logger.error("Exception Occcured : "+e.getMessage(),e);
			}
		}
		
		return response;
	}

}
