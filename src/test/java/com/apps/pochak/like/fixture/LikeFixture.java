package com.apps.pochak.like.fixture;

import com.apps.pochak.like.domain.LikeEntity;

import static com.apps.pochak.member.fixture.MemberFixture.MEMBER1;
import static com.apps.pochak.member.fixture.MemberFixture.MEMBER2;
import static com.apps.pochak.post.fixture.PostFixture.PUBLIC_POST;

public class LikeFixture {

    public static final LikeEntity LIKE1 = new LikeEntity(
            1L,
            MEMBER1,
            PUBLIC_POST
    );

    public static final LikeEntity LIKE2 = new LikeEntity(
            2L,
            MEMBER2,
            PUBLIC_POST
    );
}
