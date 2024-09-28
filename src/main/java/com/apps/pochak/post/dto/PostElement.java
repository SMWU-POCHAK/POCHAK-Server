package com.apps.pochak.post.dto;

import com.apps.pochak.post.domain.Post;
import com.apps.pochak.tag.domain.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostElement {
    private Long postId;
    private String postImage;

    public static PostElement from(final Post post) {
        return new PostElement(post.getId(), post.getPostImage());
    }

    public static PostElement from(final Tag tag) {
        return new PostElement(tag.getPost().getId(), tag.getPost().getPostImage());
    }
}
