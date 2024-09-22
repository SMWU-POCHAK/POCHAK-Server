package com.apps.pochak.memories.dto;

import com.apps.pochak.post.domain.Post;
import com.apps.pochak.tag.domain.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemoriesElement {
    private String postImage;
    private LocalDateTime postDate;

    public static MemoriesElement from(final Tag tag) {
        return new MemoriesElement(tag.getPost().getPostImage(), tag.getPost().getAllowedDate());
    }
}