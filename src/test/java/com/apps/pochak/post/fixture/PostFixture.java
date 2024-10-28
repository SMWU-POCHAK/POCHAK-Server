package com.apps.pochak.post.fixture;

import com.apps.pochak.post.domain.Post;

import java.time.LocalDateTime;

import static com.apps.pochak.member.fixture.MemberFixture.STATIC_MEMBER1;
import static com.apps.pochak.member.fixture.MemberFixture.STATIC_MEMBER2;
import static com.apps.pochak.post.domain.PostStatus.PRIVATE;
import static com.apps.pochak.post.domain.PostStatus.PUBLIC;

public class PostFixture {

    public static final String POST_IMAGE = "https://avatars.githubusercontent.com/u/163827369?s=200&v=4";
    public static final String CAPTION = "caption test";

    public static final Post STATIC_PUBLIC_POST = new Post(
            1L,
            PUBLIC,
            LocalDateTime.now(),
            STATIC_MEMBER1,
            POST_IMAGE,
            "공개 게시물 캡션입니다."
    );

    public static final Post STATIC_PRIVATE_POST = new Post(
            2L,
            PRIVATE,
            LocalDateTime.now(),
            STATIC_MEMBER2,
            POST_IMAGE,
            "아직 수락되지 않은 게시물의 캡션입니다."
    );
}
