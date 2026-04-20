package com.thinkschool.coach.model;

import java.util.List;

import lombok.Data;
import software.amazon.awssdk.services.rekognition.model.DetectFacesResponse;
import software.amazon.awssdk.services.rekognition.model.Emotion;
import software.amazon.awssdk.services.rekognition.model.FaceDetail;

@Data
public class ParsedFrame {
	
	//Face Detection
	private boolean isFaceDetected;
	
	// Head orientation (Pose)
    private Double headYaw;   
    private Double headPitch; 
    
    // Eyeball orientation (EyeDirection)
    private Double pupilYaw;   
    private Double pupilPitch; 
    
    // Physical traits
    private Boolean isSmiling;
    private Boolean isFaceOccluded; // True if hands/objects cover the face
    private Double faceCenterX;     // The horizontal center of the face on screen
    
    // Psychological traits
    private String dominantEmotion;
    
    private ParsedFrame() {
    	this.isFaceDetected = false;
    }
    
    public static ParsedFrame fromDetectFacesResponse(DetectFacesResponse response) {
    	ParsedFrame frame = new ParsedFrame();
    	
    	List<FaceDetail> faceDetailsList = response.faceDetails();
    	
    	for( FaceDetail faceDetail: faceDetailsList ) {
    		frame.isFaceDetected = true;
    		frame.headYaw = faceDetail.pose().yaw().doubleValue();
    		frame.headPitch = faceDetail.pose().pitch().doubleValue();
    		
    		frame.pupilYaw = faceDetail.eyeDirection().yaw().doubleValue();
    		frame.pupilPitch = faceDetail.eyeDirection().pitch().doubleValue();
    		
    		frame.isSmiling = faceDetail.smile().value();
    		frame.isFaceOccluded = faceDetail.faceOccluded().value();
    		
    		double left = faceDetail.boundingBox().left();
    		double width= faceDetail.boundingBox().width();
    		frame.faceCenterX = left + (width/2.0);
    		
    		frame.dominantEmotion = "UNKNOWN";
    		double maxConfidence = 0;
    		for( Emotion emotion: faceDetail.emotions() ) {
    			if( emotion.confidence() > maxConfidence ) {
    				frame.dominantEmotion = emotion.type().toString();
    				maxConfidence = emotion.confidence();
    			}
    		}
    	}
    	
    	return frame;
    }
}
