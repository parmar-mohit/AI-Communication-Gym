package com.thinkschool.coach.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import com.github.kokorin.jaffree.ffmpeg.UrlOutput;
import com.thinkschool.coach.constants.FileLocation;

@Service
public class VideoService {
	private static final Logger logger = LoggerFactory.getLogger(VideoService.class);
	
	public String convertWebmToMp4(String sessionId) {
		String webmFileName = FileLocation.SESSION_VIDEO_PREFIX+"/session-"+sessionId+".webm";
		
		File webmFile = new File(webmFileName);
		if( !webmFile.exists() ) {
			return null;
		}
		
		String mp4FileName = FileLocation.SESSION_VIDEO_PREFIX+"/session-"+sessionId+".mp4";
		
		Path inputPath = Paths.get(webmFileName);
		Path outputPath = Paths.get(mp4FileName);
		
 		FFmpeg.atPath()
        .addInput(UrlInput.fromPath(inputPath))
        .addOutput(UrlOutput.toPath(outputPath))
        .execute();
		
		return mp4FileName;
	}
	
	@Async
	public void deleteSessionVideoAndTranscript(String sessionId) {
		logger.info("Deleting Video File for Session with SessionId : "+sessionId);
		String webmFileName = FileLocation.SESSION_VIDEO_PREFIX+"/session-"+sessionId+".webm";
		String mp4FileName = FileLocation.SESSION_VIDEO_PREFIX+"/session-"+sessionId+".mp4";
		String transcriptName = FileLocation.SESSION_TRANSCRIPT_PREFIX+"/session-"+sessionId+".txt";
		
		deleteFile(webmFileName);
		deleteFile(mp4FileName);
		deleteFile(transcriptName);
	}
	
	private void deleteFile(String fileName) {
		Path path = Paths.get(fileName);

        try {
            boolean deleted = Files.deleteIfExists(path);
            if (deleted) {
                logger.info(fileName + " File deleted successfully.");
            } else {
                logger.info(fileName + "File does not exist.");
            }
        } catch (IOException e) {
            logger.error("Failed to delete file: " +fileName +" , " + e.getMessage());
        }
	}

}
