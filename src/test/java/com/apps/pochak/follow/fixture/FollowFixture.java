package com.apps.pochak.follow.fixture;

import com.apps.pochak.follow.domain.Follow;

import static com.apps.pochak.member.fixture.MemberFixture.*;

public class FollowFixture {
    public static final Follow STATIC_SEND_FOLLOW = new Follow(
            1L,
            STATIC_MEMBER1,
            STATIC_MEMBER2
    );

    public static final Follow STATIC_RECEIVE_FOLLOW = new Follow(
            2L,
            STATIC_MEMBER2,
            STATIC_MEMBER1
    );
}