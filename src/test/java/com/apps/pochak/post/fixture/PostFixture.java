package com.apps.pochak.post.fixture;

import com.apps.pochak.post.domain.Post;

import java.time.LocalDateTime;

import static com.apps.pochak.member.fixture.MemberFixture.*;
import static com.apps.pochak.post.domain.PostStatus.PRIVATE;
import static com.apps.pochak.post.domain.PostStatus.PUBLIC;

public class PostFixture {

    private static final String POST_IMAGE = "https://avatars.githubusercontent.com/u/163827369?s=200&v=4";

    public static final Post PUBLIC_POST = new Post(
            1L,
            PUBLIC,
            LocalDateTime.now(),
            MEMBER1,
            POST_IMAGE,
            "공개 게시물 캡션입니다."
    );

    public static final Post PRIVATE_POST = new Post(
            2L,
            PRIVATE,
            LocalDateTime.now(),
            MEMBER2,
            POST_IMAGE,
            "아직 수락되지 않은 게시물의 캡션입니다."
    );

    public static final Post POST_WITH_MULTI_TAG = new Post(
            3L,
            PUBLIC,
            LocalDateTime.now(),
            MEMBER3,
            POST_IMAGE,
            "공개 게시물2 캡션입니다."
    ); // 다수의 태그 생성 목적
}
