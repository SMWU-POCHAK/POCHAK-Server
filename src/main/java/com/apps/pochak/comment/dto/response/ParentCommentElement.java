package com.apps.pochak.comment.dto.response;

import com.apps.pochak.comment.domain.Comment;
import com.apps.pochak.global.Constant;
import com.apps.pochak.global.PageInfo;
import com.apps.pochak.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.apps.pochak.global.converter.ListToPageConverter.toPage;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParentCommentElement {
    private Long commentId;
    private String profileImage;
    private String handle;
    private LocalDateTime createdDate;
    private String content;
    private PageInfo childCommentPageInfo;
    private List<CommentElement> childCommentList;

    public ParentCommentElement(
            final Comment parentComment
    ) {
        this(
                parentComment,
                PageRequest.of(0, Constant.DEFAULT_PAGING_SIZE)
        );
    }

    public ParentCommentElement(
            final Comment parentComment,
            final PageRequest pageRequest
    ) {
        final Member member = parentComment.getMember();
        this.commentId = parentComment.getId();
        this.profileImage = member.getProfileImage();
        this.handle = member.getHandle();
        this.createdDate = parentComment.getCreatedDate();
        this.content = parentComment.getContent();

        final List<Comment> childCommentList = parentComment.getChildCommentList();
        final List<CommentElement> commentElementList = childCommentList.stream()
                .map(
                        CommentElement::new
                )
                .collect(Collectors.toList());

        final Page<CommentElement> commentElementPage = toPage(commentElementList, pageRequest);

        this.childCommentPageInfo = new PageInfo(commentElementPage);
        this.childCommentList = commentElementPage.getContent();
    }
}
