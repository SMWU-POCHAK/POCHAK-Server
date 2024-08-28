package com.apps.pochak.comment.fixture;

import com.apps.pochak.comment.domain.Comment;

import static com.apps.pochak.member.fixture.MemberFixture.MEMBER1;
import static com.apps.pochak.member.fixture.MemberFixture.MEMBER2;
import static com.apps.pochak.post.fixture.PostFixture.PUBLIC_POST;

public class CommentFixture {

    public static final Comment PARENT_COMMENT = new Comment(
            1L,
            "부모 댓글입니다.",
            MEMBER1,
            PUBLIC_POST
    );

    public static final Comment CHILD_COMMENT = new Comment(
            2L,
            "자식 댓글입니다.",
            MEMBER2,
            PUBLIC_POST,
            PARENT_COMMENT
    );
}
