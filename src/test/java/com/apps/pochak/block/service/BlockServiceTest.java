package com.apps.pochak.block.service;

import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.follow.domain.Follow;
import com.apps.pochak.follow.domain.repository.FollowRepository;
import com.apps.pochak.follow.service.FollowService;
import com.apps.pochak.global.ServiceTest;
import com.apps.pochak.like.domain.LikeEntity;
import com.apps.pochak.like.domain.repository.LikeRepository;
import com.apps.pochak.like.service.LikeService;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.post.domain.repository.PostRepository;
import com.apps.pochak.post.dto.request.PostUploadRequest;
import com.apps.pochak.post.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.apps.pochak.global.BaseEntityStatus.DELETED;
import static com.apps.pochak.global.BaseEntityStatus.INACTIVE;
import static com.apps.pochak.global.MockMultipartFileConverter.getMockMultipartFileOfPost;
import static com.apps.pochak.member.fixture.MemberFixture.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class BlockServiceTest extends ServiceTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    PostService postService;

    @Autowired
    BlockService blockService;

    @Autowired
    FollowService followService;

    @Autowired
    LikeService likeService;

    @Autowired
    FollowRepository followRepository;

    @Autowired
    LikeRepository likeRepository;

    private Member owner;
    private Member taggedMember1;
    private Member taggedMember2;
    private Member loginMember;

    @BeforeEach
    void setUp() {
        owner = memberRepository.save(OWNER);
        taggedMember1 = memberRepository.save(TAGGED_MEMBER1);
        taggedMember2 = memberRepository.save(TAGGED_MEMBER2);
        loginMember = memberRepository.save(LOGIN_MEMBER);
    }

    @DisplayName("차단 시, 팔로우와 좋아요가 삭제되고 게시물이 비활성화 처리된다.")
    @Test
    void block() throws Exception {
        // given
        follow(taggedMember1, owner);
        like(taggedMember1, savePublicPost());

        // when
        blockService.blockMember(
                Accessor.member(taggedMember1.getId()),
                owner.getHandle()
        );

        // then
        Follow follow = followRepository.findAll().get(0);
        LikeEntity like = likeRepository.findAll().get(0);
        Post findPost = postRepository.findAll().get(0);

        assertAll(
                () -> assertEquals(DELETED, follow.getStatus()),
                () -> assertEquals(DELETED, like.getStatus()),
                () -> assertEquals(INACTIVE, findPost.getStatus())
        );
    }

    private void follow(
            final Member sender,
            final Member receiver
    ) {
        followService.follow(
                Accessor.member(sender.getId()),
                receiver.getHandle()
        );
    }

    private void like(
            final Member sender,
            final Post post
    ) {
        likeService.likePost(
                Accessor.member(sender.getId()),
                post.getId()
        );
    }

    private Post savePublicPost() throws Exception {
        PostUploadRequest request = new PostUploadRequest(
                getMockMultipartFileOfPost(),
                "test caption",
                List.of(taggedMember1.getHandle(), taggedMember2.getHandle())
        );

        postService.savePost(
                Accessor.member(owner.getId()),
                request
        );

        Post post = postRepository.findAll().get(0);
        post.makePublic();
        return post;
    }
}