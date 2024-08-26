package com.apps.pochak.follow.fixture;

import com.apps.pochak.follow.domain.Follow;

import static com.apps.pochak.member.fixture.MemberFixture.MEMBER1;
import static com.apps.pochak.member.fixture.MemberFixture.MEMBER2;

public class FollowFixture {

    public static final Follow FOLLOW1 = new Follow(
            1L,
            MEMBER1,
            MEMBER2
    );

    public static final Follow FOLLOW2 = new Follow(
            2L,
            MEMBER2,
            MEMBER1
    );
}
