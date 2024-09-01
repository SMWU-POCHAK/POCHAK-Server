package com.apps.pochak.member.service;

import com.apps.pochak.global.s3.S3Service;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

public class ProfileImageCleanupBatch {

    private final S3Service awsS3Service;
    private final ProfileImageDeletionQueue profileImageDeletionQueue;

    public ProfileImageCleanupBatch(S3Service awsS3Service, ProfileImageDeletionQueue profileImageDeletionQueue) {
        this.awsS3Service = awsS3Service;
        this.profileImageDeletionQueue = profileImageDeletionQueue;
    }

    @Scheduled(fixedRate = 300000)
    public void cleanUpOldProfileImages() {
        List<String> imagesToDelete = profileImageDeletionQueue.getImagesToDelete();

        for (String imageUrl : imagesToDelete) {
            try {
                awsS3Service.deleteFileFromS3(imageUrl);
            } catch (Exception e) {
                profileImageDeletionQueue.add(imageUrl);
            }
        }

        profileImageDeletionQueue.clearProcessedImages(imagesToDelete);
    }
}
