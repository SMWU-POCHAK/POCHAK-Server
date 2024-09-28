package com.apps.pochak.follow.fixture;

import com.apps.pochak.follow.domain.Follow;

import static com.apps.pochak.member.fixture.MemberFixture.MEMBER1;
import static com.apps.pochak.member.fixture.MemberFixture.MEMBER2;

public class FollowFixture {
    public static final Follow SEND_FOLLOW = new Follow(
            1L,
            MEMBER1,
            MEMBER2
    );

    public static final Follow RECEIVE_FOLLOW = new Follow(
            2L,
            MEMBER2,
            MEMBER1
    );
}