package com.apps.pochak.post.dto.response;

import com.apps.pochak.member.domain.Member;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.tag.domain.Tag;
import com.apps.pochak.tag.dto.response.TagElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostPreviewResponse {
    private Long ownerId;
    private String ownerHandle;
    private String ownerProfileImage;
    private List<TagElement> tagList;
    private String postImage;

    public PostPreviewResponse(
            final Post previewPost,
            final List<Tag> tagList
    ) {
        Member owner = previewPost.getOwner();
        this.ownerId = owner.getId();
        this.ownerHandle = owner.getHandle();
        this.ownerProfileImage = owner.getProfileImage();

        this.tagList = tagList.stream().map(
                TagElement::new
        ).collect(Collectors.toList());

        this.postImage = previewPost.getPostImage();
    }
}
