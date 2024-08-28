package com.apps.pochak.tag.fixture;

import com.apps.pochak.tag.domain.Tag;

import static com.apps.pochak.member.fixture.MemberFixture.MEMBER1;
import static com.apps.pochak.member.fixture.MemberFixture.MEMBER2;
import static com.apps.pochak.post.fixture.PostFixture.PRIVATE_POST;
import static com.apps.pochak.post.fixture.PostFixture.PUBLIC_POST;

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
}
