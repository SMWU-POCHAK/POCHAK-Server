package com.apps.pochak.member.service.scheduler;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class ProfileImageDeletionQueue {
    private final ConcurrentLinkedQueue<String> imageUrlsToDelete = new ConcurrentLinkedQueue<>();

    public void add(String imageUrl) {
        imageUrlsToDelete.add(imageUrl);
    }

    public List<String> getImagesToDelete() {
        return new ArrayList<>(imageUrlsToDelete);
    }

    public void clearProcessedImages(List<String> processedImages) {
        imageUrlsToDelete.removeAll(processedImages);
    }

    public void ProfileImageDeletionQueueWhenServerShutDown() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            imageUrlsToDelete.clear();
        }));
    }
}

