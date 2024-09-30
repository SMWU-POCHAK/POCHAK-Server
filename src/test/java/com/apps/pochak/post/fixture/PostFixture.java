package com.apps.pochak.post.fixture;

import com.apps.pochak.post.domain.Post;

import java.time.LocalDateTime;

import static com.apps.pochak.member.fixture.MemberFixture.*;
import static com.apps.pochak.post.domain.PostStatus.PRIVATE;
import static com.apps.pochak.post.domain.PostStatus.PUBLIC;

public class PostFixture {

    private static final String POST_IMAGE = "https://avatars.githubusercontent.com/u/163827369?s=200&v=4";

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

    public static final Post PUBLIC_POST = Post.builder()
            .owner(OWNER)
            .postImage(POST_IMAGE)
            .caption("하나의 태그를 가진 게시물입니다.")
            .build();

    public static final Post POST_WITH_MULTI_TAG = Post.builder()
            .owner(OWNER)
            .postImage(POST_IMAGE)
            .caption("다중 태그를 가진 게시물입니다.")
            .build();
}
