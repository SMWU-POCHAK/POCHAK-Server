package com.apps.pochak.member.service.scheduler;

import com.amazonaws.AmazonServiceException;
import com.apps.pochak.global.s3.S3Service;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.apps.pochak.global.Constant.ERROR_CODE_NO_SUCH_KEY;

@Component
public class ProfileImageDeletionQueue {
    private final ConcurrentLinkedQueue<String> imageUrlsToDelete = new ConcurrentLinkedQueue<>();
    private final S3Service s3Service;

    public ProfileImageDeletionQueue(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    public void add(String imageUrl) {
        imageUrlsToDelete.add(imageUrl);
    }

    public List<String> getImagesToDelete() {
        return new ArrayList<>(imageUrlsToDelete);
    }

    public void clearProcessedImages(List<String> processedImages) {
        imageUrlsToDelete.removeAll(processedImages);
    }

    public void deleteImagesFromS3(List<String> imagesToDelete){
        for (String imageUrl : imagesToDelete) {
            try {
                s3Service.deleteFileFromS3(imageUrl);
            } catch (AmazonServiceException e) {
                if (ERROR_CODE_NO_SUCH_KEY.equals(e.getErrorCode())) {
                    //log 남기기
                }
                else{
                    add(imageUrl);
                }
            }  catch (Exception e) {
                add(imageUrl);
            }
        }

        clearProcessedImages(imagesToDelete);
    }

    public void profileImageDeletionQueueWhenServerShutDown() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            deleteImagesFromS3((List<String>) imageUrlsToDelete);
            imageUrlsToDelete.clear();
        }));
    }
}

