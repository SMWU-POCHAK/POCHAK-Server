package com.apps.pochak.post.service;

import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.comment.domain.repository.CommentRepository;
import com.apps.pochak.follow.domain.Follow;
import com.apps.pochak.follow.domain.repository.FollowRepository;
import com.apps.pochak.global.image.CloudStorageService;
import com.apps.pochak.like.domain.repository.LikeRepository;
import com.apps.pochak.login.provider.JwtProvider;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.post.domain.repository.PostRepository;
import com.apps.pochak.post.dto.PostElements;
import com.apps.pochak.post.dto.request.PostUploadRequest;
import com.apps.pochak.post.dto.response.PostDetailResponse;
import com.apps.pochak.tag.domain.Tag;
import com.apps.pochak.tag.domain.repository.TagRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.apps.pochak.global.Constant.DEFAULT_PAGING_SIZE;
import static com.apps.pochak.global.MockMultipartFileConverter.getMockMultipartFileOfPost;
import static com.apps.pochak.global.converter.ListToPageConverter.toPage;
import static com.apps.pochak.member.fixture.MemberFixture.*;
import static com.apps.pochak.post.fixture.PostFixture.CAPTION;
import static com.apps.pochak.post.fixture.PostFixture.POST_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Transactional
@SpringBootTest
class PostServiceTest {

    @Autowired
    PostService postService;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    PostRepository postRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    FollowRepository followRepository;

    @Autowired
    LikeRepository likeRepository;

    @Autowired
    CommentRepository commentRepository;

    @MockBean
    CloudStorageService cloudStorageService;

    @DisplayName("팔로우하고 있는 유저들의 게시물을 포함한 홈 탭을 조회한다")
    @Test
    void getHomeTab() throws Exception {
        // given
        Member owner = memberRepository.save(OWNER);
        Member taggedMember1 = memberRepository.save(TAGGED_MEMBER1);
        Member taggedMember2 = memberRepository.save(TAGGED_MEMBER2);
        Member loginMember = memberRepository.save(LOGIN_MEMBER);

        Post post = savePost(owner, taggedMember1, taggedMember2);
        follow(loginMember, owner);

        PostElements expected = PostElements.from(toPage(List.of(post)));

        // when
        PostElements actual = postService
                .getHomeTab(
                        Accessor.member(loginMember.getId()),
                        PageRequest.of(0, DEFAULT_PAGING_SIZE)
                );

        // then
        assertThat(actual.getPostList()).hasSize(1)
                .containsAll(expected.getPostList());
    }

    @DisplayName("게시물을 조회한다.")
    @Test
    void getPostDetail() throws Exception {
        // given
        Member owner = memberRepository.save(OWNER);
        Member taggedMember1 = memberRepository.save(TAGGED_MEMBER1);
        Member taggedMember2 = memberRepository.save(TAGGED_MEMBER2);
        Member loginMember = memberRepository.save(LOGIN_MEMBER);

        Post post = savePost(owner, taggedMember1, taggedMember2);

        PostDetailResponse expected = PostDetailResponse.of()
                .post(post)
                .tagList(List.of(new Tag(post, taggedMember1), new Tag(post, taggedMember2)))
                .isFollow(false)
                .isLike(false)
                .likeCount(0)
                .recentComment(null)
                .build();

        // when
        PostDetailResponse actual = postService
                .getPostDetail(
                        Accessor.member(loginMember.getId()),
                        post.getId()
                );

        // then
        assertEquals(expected, actual);
    }

    @DisplayName("게시물을 저장한다.")
    @Test
    void savePost() throws Exception {
        // given
        when(cloudStorageService.upload(any(), any()))
                .thenReturn("");

        Member owner = memberRepository.save(OWNER);
        Member taggedMember1 = memberRepository.save(TAGGED_MEMBER1);
        Member taggedMember2 = memberRepository.save(TAGGED_MEMBER2);

        PostUploadRequest request = new PostUploadRequest(
                getMockMultipartFileOfPost(),
                "test caption",
                List.of(taggedMember1.getHandle(), taggedMember2.getHandle())
        );

        Post expected = request.toEntity("", owner);

        // when
        postService.savePost(
                Accessor.member(owner.getId()),
                request
        );

        // then
        assertThat(postRepository.findAll()).hasSize(1);

        Post actual = postRepository.findAll().get(0);
        assertAll(
                () -> assertEquals(expected.getOwner(), actual.getOwner()),
                () -> assertEquals(expected.getPostImage(), actual.getPostImage()),
                () -> assertEquals(expected.getCaption(), actual.getCaption())
        );
    }

    private Post savePost(Member owner, Member... taggedMemberList) {
        Post post = postRepository.save(new Post(owner, POST_IMAGE, CAPTION));
        saveTags(post, taggedMemberList);
        post.makePublic();
        return post;
    }

    private void saveTags(Post post, Member... tagMemberList) {
        for (Member member : tagMemberList) {
            tagRepository.save(new Tag(post, member));
        }
    }

    private void follow(Member sender, Member receiver) {
        Follow follow = Follow.of()
                .sender(sender)
                .receiver(receiver)
                .build();
        followRepository.save(follow);
    }
}