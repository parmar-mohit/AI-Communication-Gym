package com.thinkschool.coach.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.thinkschool.coach.constants.FileLocation;
import com.thinkschool.coach.model.ParsedFrame;

import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.Attribute;
import software.amazon.awssdk.services.rekognition.model.DetectFacesRequest;
import software.amazon.awssdk.services.rekognition.model.DetectFacesResponse;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.S3Object;

@Service
public class ImageService {
	
	private static final Logger logger =  LoggerFactory.getLogger(ImageService.class);
	
	private RekognitionClient rekognitionClient;
	
	public ImageService() {
		 this.rekognitionClient = RekognitionClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build();
	}
	
	
	public List<String> extractImagesFromVideo(String videoFilePath,String sessionId) {
	    
	    // 1. Try-With-Resources guarantees these are completely destroyed when the method ends
	    try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoFilePath);
	         Java2DFrameConverter converter = new Java2DFrameConverter()) {
	        
	        grabber.start();

	        double interval = 0.5; // seconds
	        int frameNumber = 0;
	        double nextCaptureTime = 0;
	        Frame frame;
	        
	        List<String> imagePathList = new ArrayList<String>();

	        while ((frame = grabber.grabImage()) != null) {
	            
	            // grabber.getTimestamp() returns microseconds. Convert to seconds.
	            double currentTime = grabber.getTimestamp() / 1_000_000.0;

	            if (currentTime >= nextCaptureTime) {
	            	
	            	String newImagePath = FileLocation.SESSION_IMAGE_PREFIX + "session-"+sessionId + "-" + String.format("frame_%04d.png", frameNumber++);
	            	imagePathList.add(newImagePath);
	                // 2. We use the Java2DFrameConverter directly to save the image. 
	                // We completely removed the unused OpenCV 'Mat' conversion to save memory.
	                File output = new File(newImagePath);
	                ImageIO.write(converter.convert(frame), "png", output);

	                nextCaptureTime += interval;
	                logger.info("Created File with File Path : "+newImagePath);
	            }
	        }
	        
	        // 3. Cleanly stop the grabber (the try-with-resources will also call close() automatically)
	        grabber.stop();
	        
	        return imagePathList;
	    } catch (FFmpegFrameGrabber.Exception e) {
	        logger.error("FFmpeg Exception Occurred while extracting frames: " + e.getMessage(), e);
	    } catch (IOException e) {
	        logger.error("IO Exception Occurred while writing image to disk: " + e.getMessage(), e);
	    }
	    
	    return null;
	}
	
	public void deleteImages(List<String> imagePathList ) {
		for(String image: imagePathList) {
			Path path = Paths.get(image);

	        try {
	            boolean deleted = Files.deleteIfExists(path);
	            if (deleted) {
	                logger.info(image + " File deleted successfully.");
	            } else {
	                logger.info(image + "File does not exist.");
	            }
	        } catch (IOException e) {
	            logger.error("Failed to delete file: " +image +" , " + e.getMessage());
	        }
		}
	}
	
	public List<ParsedFrame> extractDataFromImages(List<String> s3ObjectList) {
		List<ParsedFrame> frameDataList = new ArrayList<ParsedFrame>();
		for(String frame: s3ObjectList ) {
			S3Object s3ObjectTarget = S3Object.builder()
	                .bucket(S3Service.SESSION_IMAGE_BUCKET)
	                .name(frame)
	                .build();

            Image targetImage = Image.builder()
                .s3Object(s3ObjectTarget)
                .build();
            
            DetectFacesRequest facesRequest = DetectFacesRequest.builder()
                    .attributes(Attribute.ALL)
                    .image(targetImage)
                    .build();
            
            logger.info("Processing Image using AWS Rekognition : "+frame);
            DetectFacesResponse facesResponse = this.rekognitionClient.detectFaces(facesRequest);
            frameDataList.add( ParsedFrame.fromDetectFacesResponse(facesResponse) );
		}
		
		return frameDataList;
	}

}
