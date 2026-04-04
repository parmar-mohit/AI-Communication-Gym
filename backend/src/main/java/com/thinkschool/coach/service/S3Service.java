package com.thinkschool.coach.service;

import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.thinkschool.coach.constants.FileLocation;

import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3Service {
	private S3Client s3Client;
	
	private static final String SESSION_VIDEO_BUCKET = "ai-communication-session-video";
	private static final String SESSION_TRANSCRIPT_BUCKET = "ai-communication-session-transcript";
	
	private static final Logger logger = LoggerFactory.getLogger(S3Service.class);
	
	public S3Service() {
		this.s3Client = S3Client.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build();
	}
	
	public void uploadSessionVideo(String fileName,String sessionId) {
		PutObjectRequest request = PutObjectRequest.builder()
                .bucket(SESSION_VIDEO_BUCKET)
                .key(sessionId)
                .build();

        s3Client.putObject(request, Paths.get(fileName));
        logger.info(fileName+" Uploaded to S3 Bucket");
	}
	
	public void uploadSessionTranscript(String sessionId) {
		String fileName = FileLocation.SESSION_TRANSCRIPT_PREFIX+"/session-"+sessionId+".txt";
		PutObjectRequest request = PutObjectRequest.builder()
                .bucket(SESSION_TRANSCRIPT_BUCKET)
                .key(sessionId)
                .build();

        s3Client.putObject(request, Paths.get(fileName));
        logger.info(fileName+" Uploaded to S3 Bucket");
	}
}
