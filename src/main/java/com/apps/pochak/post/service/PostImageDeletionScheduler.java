package com.apps.pochak.post.service;

import com.apps.pochak.global.image.CloudStorageService;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.post.domain.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PostImageDeletionScheduler {
    private final PostRepository postRepository;
    private final CloudStorageService storageService;
    public static final int DEFAULT_DELETION_SIZE = 100;

    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteExpiredAlarms() {
        final LocalDateTime expirationDate = LocalDateTime.now().minusDays(30);
        PageRequest pageRequest = PageRequest.of(0, DEFAULT_DELETION_SIZE);
        Page<Post> deletedPost;
        do {
            deletedPost = postRepository.findAllByDeletedAtBefore(
                    expirationDate,
                    pageRequest
            );
            storageService.delete(deletedPost.stream().map(Post::getPostImage).toList());
            pageRequest.next();
        } while (deletedPost.hasNext());
    }
}
