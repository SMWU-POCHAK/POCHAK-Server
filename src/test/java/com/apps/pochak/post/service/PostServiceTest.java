package com.apps.pochak.post.service;

import com.apps.pochak.alarm.domain.TagAlarm;
import com.apps.pochak.alarm.domain.repository.AlarmRepository;
import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.comment.domain.repository.CommentRepository;
import com.apps.pochak.follow.domain.Follow;
import com.apps.pochak.follow.domain.repository.FollowRepository;
import com.apps.pochak.global.api_payload.exception.GeneralException;
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
import com.apps.pochak.post.dto.response.PostPreviewResponse;
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

import static com.apps.pochak.global.BaseEntityStatus.DELETED;
import static com.apps.pochak.global.Constant.DEFAULT_PAGING_SIZE;
import static com.apps.pochak.global.MockMultipartFileConverter.getMockMultipartFileOfPost;
import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.NOT_YOUR_POST;
import static com.apps.pochak.global.converter.ListToPageConverter.toPage;
import static com.apps.pochak.member.fixture.MemberFixture.*;
import static com.apps.pochak.post.fixture.PostFixture.CAPTION;
import static com.apps.pochak.post.fixture.PostFixture.POST_IMAGE;
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

    @Autowired
    private AlarmRepository alarmRepository;

    @DisplayName("홈 탭을 조회한다")
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

    @DisplayName("게시물을 삭제한다.")
    @Test
    void deletePost() throws Exception {
        // given
        Member owner = memberRepository.save(OWNER);
        Member taggedMember1 = memberRepository.save(TAGGED_MEMBER1);
        Member taggedMember2 = memberRepository.save(TAGGED_MEMBER2);

        Post post = savePost(owner, taggedMember1, taggedMember2);

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
        Member owner = memberRepository.save(OWNER);
        Member taggedMember1 = memberRepository.save(TAGGED_MEMBER1);
        Member taggedMember2 = memberRepository.save(TAGGED_MEMBER2);
        Member loginMember = memberRepository.save(LOGIN_MEMBER);

        Post post = savePost(owner, taggedMember1, taggedMember2);

        // when, then
        GeneralException exception = assertThrows(
                GeneralException.class,
                () -> postService.deletePost(
                        Accessor.member(loginMember.getId()),
                        post.getId()
                )
        );

        assertEquals(NOT_YOUR_POST, exception.getCode());
    }

    @DisplayName("탐색탭을 조회한다.")
    @Test
    void getSearchTab() throws Exception {
        // given
        Member owner = memberRepository.save(OWNER);
        Member taggedMember1 = memberRepository.save(TAGGED_MEMBER1);
        Member taggedMember2 = memberRepository.save(TAGGED_MEMBER2);
        Member loginMember = memberRepository.save(LOGIN_MEMBER);

        Post post = savePost(owner, taggedMember1, taggedMember2);

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

        postService.savePost(
                Accessor.member(owner.getId()),
                request
        );

        Post post = postRepository.findAll().get(0);
        List<Tag> tagList = tagRepository.findTagsByPost(post);
        PostPreviewResponse expected = new PostPreviewResponse(post, tagList);

        TagAlarm alarm = (TagAlarm) alarmRepository.findAll().get(0);

        // when
        PostPreviewResponse actual = postService.getPreviewPost(
                Accessor.member(alarm.getReceiver().getId()),
                alarm.getId()
        );

        // then
        assertEquals(expected, actual);
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