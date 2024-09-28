package com.apps.pochak.tag.fixture;

import com.apps.pochak.tag.domain.Tag;

import static com.apps.pochak.member.fixture.MemberFixture.MEMBER1;
import static com.apps.pochak.member.fixture.MemberFixture.MEMBER2;
import static com.apps.pochak.post.fixture.PostFixture.*;

public class TagFixture {
    public static final Tag APPROVED_TAG = new Tag(
            1L,
            PUBLIC_POST,
            MEMBER2,
            true
    );
    public static final Tag WAITING_TAG = new Tag(
            2L,
            PRIVATE_POST,
            MEMBER1,
            false
    );
    public static final Tag TAG1_WITH_ONE_POST = new Tag(
            3L,
            POST_WITH_MULTI_TAG,
            MEMBER1,
            true
    );
    public static final Tag TAG2_WITH_ONE_POST = new Tag(
            4L,
            POST_WITH_MULTI_TAG,
            MEMBER2,
            true
    );
}
