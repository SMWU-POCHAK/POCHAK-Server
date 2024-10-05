package com.apps.pochak.tag.fixture;

import com.apps.pochak.tag.domain.Tag;

import static com.apps.pochak.member.fixture.MemberFixture.*;
import static com.apps.pochak.post.fixture.PostFixture.*;

public class TagFixture {
    public static final Tag STATIC_APPROVED_TAG = new Tag(
            1L,
            STATIC_PUBLIC_POST,
            STATIC_MEMBER2,
            true
    );

    public static final Tag STATIC_WAITING_TAG = new Tag(
            2L,
            STATIC_PRIVATE_POST,
            STATIC_MEMBER1,
            false
    );

    public static final Tag APPROVAL_TAG = Tag.builder()
            .post(PUBLIC_POST)
            .member(TAGGED_MEMBER1)
            .build();

    public static final Tag TAG1_WITH_ONE_POST = Tag.builder()
            .post(POST_WITH_MULTI_TAG)
            .member(TAGGED_MEMBER1)
            .build();

    public static final Tag TAG2_WITH_ONE_POST = Tag.builder()
            .post(POST_WITH_MULTI_TAG)
            .member(TAGGED_MEMBER2)
            .build();
}
