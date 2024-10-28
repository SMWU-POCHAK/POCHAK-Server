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
}
