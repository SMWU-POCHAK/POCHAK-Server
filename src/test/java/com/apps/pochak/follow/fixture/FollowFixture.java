package com.apps.pochak.follow.fixture;

import com.apps.pochak.follow.domain.Follow;
import com.apps.pochak.member.domain.Member;

import static com.apps.pochak.member.fixture.MemberFixture.STATIC_MEMBER1;
import static com.apps.pochak.member.fixture.MemberFixture.STATIC_MEMBER2;

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

    public static Follow buildFollow(Member sender, Member receiver) {
        return Follow.of()
                .sender(sender)
                .receiver(receiver)
                .build();
    }
}