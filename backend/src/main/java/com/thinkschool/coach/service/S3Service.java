package com.thinkschool.coach.service;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.thinkschool.coach.constants.FileLocation;

import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

@Service
public class S3Service {
	private S3Client s3Client;
	
	private static final String SESSION_VIDEO_BUCKET = System.getenv("S3_SESSION_VIDEO_BUCKET_ID");
	private static final String SESSION_TRANSCRIPT_BUCKET = System.getenv("S3_SESSION_TRANSCRIPT_BUCKET_ID");
	public static final String SESSION_IMAGE_BUCKET = System.getenv("S3_SESSION_IMAGE_BUCKET_ID");
	
	private static final Logger logger = LoggerFactory.getLogger(S3Service.class);
	
	public S3Service() {
		this.s3Client = S3Client.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build();
	}
	
	public void uploadSessionVideo(String fileName,String sessionId) {
		logger.info("Uploading Session Video to Bucket : "+sessionId);
		PutObjectRequest request = PutObjectRequest.builder()
                .bucket(SESSION_VIDEO_BUCKET)
                .key(sessionId)
                .build();

        s3Client.putObject(request, Paths.get(fileName));
        logger.info(fileName+" Uploaded to S3 Bucket");
	}
	
	public void uploadSessionTranscript(String sessionId) {
		logger.info("Uploading Session Transcript to Bucket : "+sessionId);
		
		String fileName = FileLocation.SESSION_TRANSCRIPT_PREFIX+"/session-"+sessionId+".txt";
		PutObjectRequest request = PutObjectRequest.builder()
                .bucket(SESSION_TRANSCRIPT_BUCKET)
                .key(sessionId)
                .build();

        s3Client.putObject(request, Paths.get(fileName));
        logger.info(fileName+" Uploaded to S3 Bucket");
	}
	
	public void uploadSessionImages(List<String> filePathList) {
		logger.info("Uploading Session Images to Bucket");
		for( String fileName : filePathList ) {
			String[] pathSplit = fileName.split("/"); 
			String key = pathSplit[pathSplit.length-1];
			PutObjectRequest request = PutObjectRequest.builder()
	                .bucket(SESSION_IMAGE_BUCKET)
	                .key(key)
	                .build();
	
	        s3Client.putObject(request, Paths.get(fileName));
	        logger.info(fileName+" Uploaded to S3 Bucket");
		}
	}
	
	public List<String> getKeyForSessionImages(String sessionId){
		logger.info("Fetching Object Key of Image with mentioned SessionId : "+sessionId);
		
		String prefix = "session-"+sessionId+"-frame";
		
		List<String> s3ObjectList = new ArrayList<String>();
		ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(SESSION_IMAGE_BUCKET)
                .prefix(prefix)
                .build();

        ListObjectsV2Response response;

        do {
            response = s3Client.listObjectsV2(request);

            boolean canBreak = false;
            for (S3Object obj : response.contents()) {
            	s3ObjectList.add(obj.key());
            }
            
            if( canBreak ) {
            	break;
            }

            request = request.toBuilder()
                    .continuationToken(response.nextContinuationToken())
                    .build();

        } while (response.isTruncated());
        
        return s3ObjectList;
    }
	
	public boolean doesSessionVideoExist(String sessionId) {
		try {
	        HeadObjectRequest request = HeadObjectRequest.builder()
	                .bucket(SESSION_VIDEO_BUCKET)
	                .key(sessionId)
	                .build();

	        this.s3Client.headObject(request);
	        return true;
	    } catch (NoSuchKeyException e) {
	        return false;

	    } catch (S3Exception e) {
	        if (e.statusCode() == 404) {
	            return false;
	        }
	        logger.error("Exception Occured : "+e.getMessage(),e);
	        throw e;
	    }
	}
}
