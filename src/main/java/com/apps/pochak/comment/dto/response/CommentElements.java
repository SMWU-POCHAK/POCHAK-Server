package com.apps.pochak.comment.dto.response;

import com.apps.pochak.comment.domain.Comment;
import com.apps.pochak.global.util.PageInfo;
import com.apps.pochak.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentElements {
    private PageInfo parentCommentPageInfo;
    private List<ParentCommentElement> parentCommentList;
    private String loginMemberProfileImage;

    public CommentElements(
            final Member loginMember,
            final Page<Comment> parentCommentPage,
            final List<Comment> childComments
    ) {
        parentCommentPageInfo = new PageInfo(parentCommentPage);

        parentCommentList = new ArrayList<>();
        for (Comment parentComment : parentCommentPage.getContent()) {
            List<Comment> childCommentList = new ArrayList<>();
            for (Comment childComment : childComments) {
                if (Objects.equals(parentComment.getId(), childComment.getParentComment().getId())) {
                    childCommentList.add(childComment);
                    break;
                }
            }
            parentCommentList.add(new ParentCommentElement(parentComment, childCommentList));
        }

        loginMemberProfileImage = loginMember.getProfileImage();
    }
}