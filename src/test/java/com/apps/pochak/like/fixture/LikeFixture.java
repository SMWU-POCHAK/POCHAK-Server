package com.apps.pochak.like.fixture;

import com.apps.pochak.like.domain.LikeEntity;

import static com.apps.pochak.member.fixture.MemberFixture.*;
import static com.apps.pochak.post.fixture.PostFixture.PUBLIC_POST;
import static com.apps.pochak.post.fixture.PostFixture.STATIC_PUBLIC_POST;

public class LikeFixture {

    public static final LikeEntity STATIC_LIKE1 = new LikeEntity(
            1L,
            STATIC_MEMBER1,
            STATIC_PUBLIC_POST
    );

    public static final LikeEntity STATIC_LIKE2 = new LikeEntity(
            2L,
            STATIC_MEMBER2,
            STATIC_PUBLIC_POST
    );

    public static final LikeEntity LIKE = new LikeEntity(
            LOGIN_MEMBER,
            PUBLIC_POST
    );
}
