package com.apps.pochak.memories.service;

import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.follow.domain.Follow;
import com.apps.pochak.follow.domain.repository.FollowRepository;
import com.apps.pochak.follow.service.FollowService;
import com.apps.pochak.global.ServiceTest;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.memories.domain.MemoriesType;
import com.apps.pochak.memories.dto.response.MemoriesPostResponse;
import com.apps.pochak.memories.dto.response.MemoriesPreviewResponse;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.post.domain.repository.PostRepository;
import com.apps.pochak.post.fixture.PostFixture;
import com.apps.pochak.tag.domain.Tag;
import com.apps.pochak.tag.domain.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static com.apps.pochak.global.Constant.DEFAULT_PAGING_SIZE;
import static com.apps.pochak.member.fixture.MemberFixture.*;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class MemoriesServiceTest extends ServiceTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    FollowRepository followRepository;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    FollowService followService;

    @Autowired
    MemoriesService memoriesService;

    private Member owner;
    private Member loginMember;
    private Member counterpart;
    private Follow follow;
    private Follow followed;
    private Post tagPost;
    private Tag tag;
    private Post taggedPost;
    private Tag tagged;
    private Post multiTagPost;
    private Tag taggedwith;


    @BeforeEach
    void setUp() {
        owner = memberRepository.save(OWNER);
        loginMember = memberRepository.save(TAGGED_MEMBER1);
        counterpart = memberRepository.save(TAGGED_MEMBER2);

        follow = follow(loginMember, counterpart);
        followed = follow(counterpart, loginMember);

        tagPost = postRepository.save(PostFixture.get(loginMember));
        tag = tagRepository.save(new Tag(tagPost, counterpart));
        tagPost.makePublic();

        taggedPost = postRepository.save(PostFixture.get(counterpart));
        tagged = tagRepository.save(new Tag(taggedPost, loginMember));
        taggedPost.makePublic();

        multiTagPost = postRepository.save(PostFixture.get(owner));
        taggedwith = tagRepository.save(new Tag(multiTagPost, loginMember));
        tagRepository.save(new Tag(multiTagPost, counterpart));
        multiTagPost.makePublic();
    }

    @DisplayName("추억 프리뷰 페이지를 조회한다.")
    @Test
    void getMemories() {
        // given
        final Map<MemoriesType, Tag> tags = new HashMap<>();
        tags.put(MemoriesType.FirstPochaked, tagged);
        tags.put(MemoriesType.FirstPochak, tag);
        tags.put(MemoriesType.FirstBonded, taggedwith);
        tags.put(MemoriesType.LatestPost, taggedwith);
        tags.put(MemoriesType.Post1YearAgo, null);

        MemoriesPreviewResponse expected = MemoriesPreviewResponse.of()
                .loginMember(loginMember)
                .member(counterpart)
                .follow(follow)
                .followed(followed)
                .countTag(1L)
                .countTaggedWith(1L)
                .countTagged(1L)
                .tags(tags)
                .build();

        // when
        MemoriesPreviewResponse actual = memoriesService.getMemories(
                Accessor.member(loginMember.getId()),
                counterpart.getHandle()
        );

        // then
        assertEquals(expected, actual);
        assertAll(
                () -> assertEquals(follow.getLastModifiedDate(), actual.getFollowDate()),
                () -> assertEquals(followed.getLastModifiedDate(), actual.getFollowedDate()),
                () -> assertEquals(tagPost.getId(), actual.getMemories().get(MemoriesType.FirstPochak).getPostId()),
                () -> assertEquals(taggedPost.getId(), actual.getMemories().get(MemoriesType.FirstPochaked).getPostId()),
                () -> assertEquals(multiTagPost.getId(), actual.getMemories().get(MemoriesType.FirstBonded).getPostId()),
                () -> assertEquals(multiTagPost.getId(), actual.getMemories().get(MemoriesType.LatestPost).getPostId()),
                () -> assertNull(actual.getMemories().get(MemoriesType.Post1YearAgo).getPostId())
        );
    }

    @DisplayName("상대를 포착한 게시글들을 조회한다.")
    @Test
    void getPochak() {
        // when
        MemoriesPostResponse memoriesPostResponse = memoriesService.getPochak(
                Accessor.member(loginMember.getId()),
                counterpart.getHandle(),
                PageRequest.of(0, DEFAULT_PAGING_SIZE)
        );

        // then
        assertEquals(tagPost.getId(), memoriesPostResponse.getPostList().get(0).getPostId());
    }

    @DisplayName("상대를 포착된 게시글들을 조회한다.")
    @Test
    void getPochaked() {
        // when
        MemoriesPostResponse memoriesPostResponse = memoriesService.getPochaked(
                Accessor.member(loginMember.getId()),
                counterpart.getHandle(),
                PageRequest.of(0, DEFAULT_PAGING_SIZE)
        );

        // then
        assertEquals(taggedPost.getId(), memoriesPostResponse.getPostList().get(0).getPostId());
    }

    @DisplayName("상대와 함께 포착된 게시글들을 조회한다.")
    @Test
    void getBonded() {
        // when
        MemoriesPostResponse memoriesPostResponse = memoriesService.getBonded(
                Accessor.member(loginMember.getId()),
                counterpart.getHandle(),
                PageRequest.of(0, DEFAULT_PAGING_SIZE)
        );

        // then
        assertEquals(multiTagPost.getId(), memoriesPostResponse.getPostList().get(0).getPostId());
    }

    @DisplayName("상대방과 맞팔 상태를 확인한다.")
    @Test
    void getF4FStatus() {
        // when
        Boolean isF4F = memoriesService.getF4FStatus(
                Accessor.member(loginMember.getId()),
                counterpart.getHandle()
        );

        // then
        assertEquals(true, isF4F);
    }

    private Follow follow(Member sender, Member receiver) {
        final Follow newFollow = Follow.of()
                .sender(sender)
                .receiver(receiver)
                .build();
        return followRepository.save(newFollow);
    }
}