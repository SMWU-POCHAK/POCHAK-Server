package com.apps.pochak.comment.fixture;

import com.apps.pochak.comment.domain.Comment;

import static com.apps.pochak.member.fixture.MemberFixture.STATIC_MEMBER1;
import static com.apps.pochak.post.fixture.PostFixture.STATIC_PUBLIC_POST;

public class CommentFixture {

    public static final Comment STATIC_PARENT_COMMENT = new Comment(
            1L,
            "부모 댓글입니다.",
            STATIC_MEMBER1,
            STATIC_PUBLIC_POST
    );

    public static final Comment STATIC_CHILD_COMMENT = new Comment(
            2L,
            "자식 댓글입니다.",
            STATIC_MEMBER1,
            STATIC_PUBLIC_POST,
            STATIC_PARENT_COMMENT
    );
}
