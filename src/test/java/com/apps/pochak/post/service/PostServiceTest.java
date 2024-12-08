package com.apps.pochak.post.service;

import com.apps.pochak.alarm.domain.TagAlarm;
import com.apps.pochak.alarm.domain.repository.AlarmRepository;
import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.follow.domain.Follow;
import com.apps.pochak.follow.domain.repository.FollowRepository;
import com.apps.pochak.follow.service.FollowService;
import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.global.image.CloudStorageService;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.post.domain.repository.PostRepository;
import com.apps.pochak.post.dto.PostElements;
import com.apps.pochak.post.dto.request.PostUploadRequest;
import com.apps.pochak.post.dto.response.PostDetailResponse;
import com.apps.pochak.post.dto.response.PostPreviewResponse;
import com.apps.pochak.tag.domain.Tag;
import com.apps.pochak.tag.domain.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.apps.pochak.global.BaseEntityStatus.DELETED;
import static com.apps.pochak.global.Constant.DEFAULT_PAGING_SIZE;
import static com.apps.pochak.global.MockMultipartFileConverter.getMockMultipartFileOfPost;
import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.NO_DELETE_PERMISSION;
import static com.apps.pochak.global.converter.ListToPageConverter.toPage;
import static com.apps.pochak.member.fixture.MemberFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Transactional
@SpringBootTest
class PostServiceTest {

    @Autowired
    PostService postService;

    @Autowired
    FollowService followService;

    @MockBean
    CloudStorageService cloudStorageService;

    @Autowired
    PostRepository postRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    AlarmRepository alarmRepository;

    private Member owner;
    private Member taggedMember1;
    private Member taggedMember2;
    private Member loginMember;
    @Autowired
    private FollowRepository followRepository;

    @BeforeEach
    void setUp() {
        owner = memberRepository.save(OWNER);
        taggedMember1 = memberRepository.save(TAGGED_MEMBER1);
        taggedMember2 = memberRepository.save(TAGGED_MEMBER2);
        loginMember = memberRepository.save(LOGIN_MEMBER);
    }

    @DisplayName("홈 탭을 조회한다")
    @Test
    void getHomeTab() throws Exception {
        // given
        Post post = savePublicPost();
        follow(loginMember, owner);

        List<Follow> all = followRepository.findAll();

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
        Post post = savePublicPost();

        List<Tag> tagList = tagRepository.findTagsByPost(post);
        PostDetailResponse expected = PostDetailResponse.of()
                .post(post)
                .tagList(tagList)
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
    void savePostTest() throws Exception {
        // given
        when(cloudStorageService.upload(any(), any()))
                .thenReturn("");

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

    @DisplayName("게시물을 삭제한다.")
    @Test
    void deletePost() throws Exception {
        // given
        Post post = savePublicPost();

        // when
        postService.deletePost(
                Accessor.member(owner.getId()),
                post.getId()
        );

        // then
        assertThat(postRepository.findAll()).hasSize(1);
        Post actual = postRepository.findAll().get(0);
        assertEquals(DELETED, actual.getStatus());
    }

    @DisplayName("게시물 삭제 권한이 없을 시 예외가 발생한다.")
    @Test
    void deletePost_WithoutAuthority() throws Exception {
        // given
        Post post = savePublicPost();

        // when, then
        GeneralException exception = assertThrows(
                GeneralException.class,
                () -> postService.deletePost(
                        Accessor.member(loginMember.getId()),
                        post.getId()
                )
        );

        assertEquals(NO_DELETE_PERMISSION, exception.getCode());
    }

    @DisplayName("탐색탭을 조회한다.")
    @Test
    void getSearchTab() throws Exception {
        // given
        Post post = savePublicPost();
        PostElements expected = PostElements.from(toPage(List.of(post)));

        // when
        PostElements actual = postService
                .getSearchTab(
                        Accessor.member(loginMember.getId()),
                        PageRequest.of(0, DEFAULT_PAGING_SIZE)
                );

        // then
        assertThat(actual.getPostList()).hasSize(1)
                .containsAll(expected.getPostList());
    }

    @DisplayName("게시물 미리보기를 조회한다.")
    @Test
    void getPreviewPost() throws Exception {
        // given
        Post post = savePost();
        List<Tag> tagList = tagRepository.findTagsByPost(post);
        PostPreviewResponse expected = new PostPreviewResponse(post, tagList);

        // when
        TagAlarm alarm = (TagAlarm) alarmRepository.findAll().get(0);
        PostPreviewResponse actual = postService.getPreviewPost(
                Accessor.member(alarm.getReceiver().getId()),
                alarm.getId()
        );

        // then
        assertEquals(expected, actual);
    }

    private Post savePost() throws Exception {
        when(cloudStorageService.upload(any(), any()))
                .thenReturn("");

        PostUploadRequest request = new PostUploadRequest(
                getMockMultipartFileOfPost(),
                "test caption",
                List.of(taggedMember1.getHandle(), taggedMember2.getHandle())
        );

        postService.savePost(
                Accessor.member(owner.getId()),
                request
        );

        return postRepository.findAll().get(0);
    }

    private Post savePublicPost() throws Exception {
        Post post = savePost();
        post.makePublic();
        return post;
    }

    private void follow(Member sender, Member receiver) {
        followService.follow(Accessor.member(sender.getId()), receiver.getHandle());
    }
}