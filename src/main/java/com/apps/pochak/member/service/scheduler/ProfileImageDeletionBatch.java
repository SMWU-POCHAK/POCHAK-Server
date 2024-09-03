package com.apps.pochak.member.service.scheduler;

import com.amazonaws.AmazonServiceException;
import com.apps.pochak.global.s3.S3Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.util.List;

@Component
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

        for (String imageUrl : imagesToDelete) {
            try {
                awsS3Service.deleteFileFromS3(imageUrl);
            } catch (AmazonServiceException e) {
                if ("NoSuchKey".equals(e.getErrorCode())) {
                    //log 남기기
                }
                else{
                    profileImageDeletionQueue.add(imageUrl);
                }
            }  catch (Exception e) {
                profileImageDeletionQueue.add(imageUrl);
            }
        }

        profileImageDeletionQueue.clearProcessedImages(imagesToDelete);
    }
}
