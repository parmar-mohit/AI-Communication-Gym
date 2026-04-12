package com.thinkschool.coach.cron;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.thinkschool.coach.constants.FileLocation;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class FileCleanCron{
	
	private static final Logger logger  = LoggerFactory.getLogger(FileCleanCron.class);

    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteOldFiles() {
    	deleteFileFromDirectory(FileLocation.SESSION_VIDEO_PREFIX);
    	deleteFileFromDirectory(FileLocation.SESSION_TRANSCRIPT_PREFIX);
    	deleteFileFromDirectory(FileLocation.SESSION_IMAGE_PREFIX);
    }
    
    private void deleteFileFromDirectory(String directoryPath) {
        Path dir = Paths.get(directoryPath);

        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            System.out.println("Invalid directory: " + directoryPath);
            return;
        }

        Instant cutoffTime = Instant.now().minus(24, ChronoUnit.HOURS);

        try {
            Files.list(dir).forEach(path -> {
                try {
                    if (Files.isRegularFile(path)) {

                        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);

                        Instant creationTime = attrs.creationTime().toInstant();
                        Instant modifiedTime = attrs.lastModifiedTime().toInstant();

                        if (creationTime.isBefore(cutoffTime) || modifiedTime.isBefore(cutoffTime)) {
                            Files.deleteIfExists(path);
                            logger.info("Deleted old file: " + path);
                        }
                    }
                } catch (IOException e) {
                    logger.error("Error processing file: " + path + " -> " + e.getMessage());
                }
            });
        } catch (IOException e) {
        	logger.error("Error reading directory: " + e.getMessage());
        }
    }
}
