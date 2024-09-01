package com.apps.pochak.member.service;

import java.util.ArrayList;
import java.util.List;

public class ProfileImageDeletionQueue {

    private final List<String> imageUrlsToDelete = new ArrayList<>();

    public void add(String imageUrl) {
        synchronized (imageUrlsToDelete) {
            imageUrlsToDelete.add(imageUrl);
        }
    }

    public List<String> getImagesToDelete() {
        synchronized (imageUrlsToDelete) {
            return new ArrayList<>(imageUrlsToDelete);
        }
    }

    public void clearProcessedImages(List<String> processedImages) {
        synchronized (imageUrlsToDelete) {
            imageUrlsToDelete.removeAll(processedImages);
        }
    }
}
