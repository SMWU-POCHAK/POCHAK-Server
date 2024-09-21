package com.apps.pochak.comment.dto.response;

import com.apps.pochak.comment.domain.Comment;
import com.apps.pochak.global.util.PageInfo;
import com.apps.pochak.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentElements {
    private PageInfo parentCommentPageInfo;
    private List<ParentCommentElement> parentCommentList;
    private String loginMemberProfileImage;

    public CommentElements(
            final Member loginMember, final Page<Comment> parentCommentPage
    ) {
        parentCommentPageInfo = new PageInfo(parentCommentPage);
        parentCommentList = parentCommentPage.getContent().stream().map(
                ParentCommentElement::new
        ).collect(Collectors.toList());
        loginMemberProfileImage = loginMember.getProfileImage();
    }
}
