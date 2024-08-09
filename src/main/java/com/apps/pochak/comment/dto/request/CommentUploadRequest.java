package com.apps.pochak.comment.dto.request;

import com.apps.pochak.comment.domain.Comment;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.post.domain.Post;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentUploadRequest {
    @NotNull(message = "댓글 내용은 필수로 전달해야 합니다.")
    private String content;

    private Long parentCommentId;

    public Comment toEntity(
            final Member member,
            final Post post
    ) {
        return new Comment(this.content, member, post);
    }

    public Comment toEntity(
            final Member member,
            final Post post,
            final Comment parentComment
    ) {
        return new Comment(this.content, member, post, parentComment);
    }

    public boolean checkChildComment() {
        return getParentCommentId() != null;
    }
}
