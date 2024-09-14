package com.apps.pochak.member.service.scheduler;

import com.amazonaws.AmazonServiceException;
import com.apps.pochak.global.s3.S3Service;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.util.List;

@Component
@EnableScheduling
public class ProfileImageDeletionBatch {

    private final S3Service awsS3Service;
    private final ProfileImageDeletionQueue profileImageDeletionQueue;

    public ProfileImageDeletionBatch(S3Service awsS3Service, ProfileImageDeletionQueue profileImageDeletionQueue) {
        this.awsS3Service = awsS3Service;
        this.profileImageDeletionQueue = profileImageDeletionQueue;
    }

    @Scheduled(fixedRate = 300000)
    public void cleanUpOldProfileImages() {
        List<String> imagesToDelete = profileImageDeletionQueue.getImagesToDelete();
        profileImageDeletionQueue.deleteImagesFromS3(imagesToDelete);
    }
}
