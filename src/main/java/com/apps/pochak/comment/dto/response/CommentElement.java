package com.apps.pochak.comment.dto.response;

import com.apps.pochak.comment.domain.Comment;
import com.apps.pochak.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentElement {
    private Long commentId;
    private Long memberId;
    private String profileImage;
    private String handle;
    private LocalDateTime createdDate;
    private String content;

    @Builder(builderMethodName = "from")
    public CommentElement(final Comment comment) {
        final Member member = comment.getMember();
        this.commentId = comment.getId();
        this.memberId = member.getId();
        this.profileImage = member.getProfileImage();
        this.handle = member.getHandle();
        this.createdDate = comment.getCreatedDate();
        this.content = comment.getContent();
    }
}
