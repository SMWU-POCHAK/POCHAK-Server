package com.apps.pochak.post.domain.repository;

import com.apps.pochak.block.domain.Block;
import com.apps.pochak.block.domain.repository.BlockRepository;
import com.apps.pochak.follow.domain.Follow;
import com.apps.pochak.follow.domain.repository.FollowRepository;
import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.member.fixture.MemberFixture;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.tag.domain.Tag;
import com.apps.pochak.tag.domain.repository.TagRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static com.apps.pochak.global.Constant.DEFAULT_PAGING_SIZE;
import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.BLOCKED_POST;
import static com.apps.pochak.post.fixture.PostFixture.CAPTION;
import static com.apps.pochak.post.fixture.PostFixture.POST_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class PostCustomRepositoryTest {

    private static final Member OWNER = MemberFixture.OWNER;
    private static final Member TAGGED_MEMBER1 = MemberFixture.TAGGED_MEMBER1;
    private static final Member TAGGED_MEMBER2 = MemberFixture.TAGGED_MEMBER2;
    private static final Member LOGIN_MEMBER = MemberFixture.LOGIN_MEMBER;

    @Autowired
    PostCustomRepository postCustomRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    BlockRepository blockRepository;

    @Autowired
    FollowRepository followRepository;

    @AfterEach
    void deleteAll() {
        postRepository.deleteAll();
        memberRepository.deleteAll();
        tagRepository.deleteAll();
        blockRepository.deleteAll();
        followRepository.deleteAll();
    }

    @DisplayName("[게시물 id 조회] 차단된 게시물을 제외한 게시물이 조회된다.")
    @Test
    void findById() throws Exception {
        //given
        Post post = savePost(
                memberRepository.save(OWNER),
                memberRepository.save(TAGGED_MEMBER1),
                memberRepository.save(TAGGED_MEMBER2)
        );
        Member loginMember = memberRepository.save(LOGIN_MEMBER);

        //when
        Post findPost = postCustomRepository.findPostByIdWithoutBlockPost(post.getId(), loginMember.getId());

        //then
        assertEquals(post.getId(), findPost.getId());
    }

    @DisplayName("[게시물 id 조회] 유효한 id가 없는 경우 조회되지 않는다.")
    @Test
    void findById_WhenIdIsInvalid() throws Exception {
        //given
        Post post = savePost(
                memberRepository.save(OWNER),
                memberRepository.save(TAGGED_MEMBER1),
                memberRepository.save(TAGGED_MEMBER2)
        );
        Member loginMember = memberRepository.save(LOGIN_MEMBER);
        Long invalidPostId = 0L;

        //when, then
        GeneralException exception = assertThrows(
                GeneralException.class,
                () -> postCustomRepository.findPostByIdWithoutBlockPost(invalidPostId, loginMember.getId())
        );

        assertEquals(BLOCKED_POST, exception.getCode());
    }

    @DisplayName("[게시물 id 조회] 업로더가 현재 유저를 차단하였다면 조회되지 않는다.")
    @Test
    void findById_WhenOwnerBlockLoginMember() throws Exception {
        //given
        Post post = savePost(
                memberRepository.save(OWNER),
                memberRepository.save(TAGGED_MEMBER1),
                memberRepository.save(TAGGED_MEMBER2)
        );
        Member loginMember = memberRepository.save(LOGIN_MEMBER);

        block(post.getOwner(), loginMember);

        //when, then
        GeneralException exception = assertThrows(
                GeneralException.class,
                () -> postCustomRepository.findPostByIdWithoutBlockPost(post.getId(), loginMember.getId())
        );

        assertEquals(BLOCKED_POST, exception.getCode());
    }

    @DisplayName("[게시물 id 조회] 태그된 사람이 현재 유저를 차단하였다면 조회되지 않는다.")
    @Test
    void findById_WhenTaggedMemberBlockLoginMember() throws Exception {
        //given
        Member owner = memberRepository.save(OWNER);
        Member taggedMember1 = memberRepository.save(TAGGED_MEMBER1);
        Member taggedMember2 = memberRepository.save(TAGGED_MEMBER2);
        Post post = savePost(owner, taggedMember1, taggedMember2);
        Member loginMember = memberRepository.save(LOGIN_MEMBER);

        block(taggedMember1, loginMember);

        //when, then
        GeneralException exception = assertThrows(
                GeneralException.class,
                () -> postCustomRepository.findPostByIdWithoutBlockPost(post.getId(), loginMember.getId())
        );

        assertEquals(BLOCKED_POST, exception.getCode());
    }

    @DisplayName("[게시물 id 조회] 현재 유저가 게시물 업로더를 차단하였다면 조회되지 않는다.")
    @Test
    void findById_WhenLoginMemberBlockOwner() throws Exception {
        //given
        Member owner = memberRepository.save(OWNER);
        Member taggedMember1 = memberRepository.save(TAGGED_MEMBER1);
        Member taggedMember2 = memberRepository.save(TAGGED_MEMBER2);
        Post post = savePost(owner, taggedMember1, taggedMember2);
        Member loginMember = memberRepository.save(LOGIN_MEMBER);

        block(loginMember, post.getOwner());

        //when,then
        GeneralException exception = assertThrows(
                GeneralException.class,
                () -> postCustomRepository.findPostByIdWithoutBlockPost(post.getId(), loginMember.getId())
        );

        assertEquals(BLOCKED_POST, exception.getCode());
    }

    @DisplayName("[게시물 id 조회] 현재 유저가 게시물 업로더 중 한명이라도 차단하였다면 조회되지 않는다.")
    @Test
    void findPostById_WhenLoginMemberBlockTaggedMember() throws Exception {
        //given
        Member owner = memberRepository.save(OWNER);
        Member taggedMember1 = memberRepository.save(TAGGED_MEMBER1);
        Member taggedMember2 = memberRepository.save(TAGGED_MEMBER2);
        Post post = savePost(owner, taggedMember1, taggedMember2);
        Member loginMember = memberRepository.save(LOGIN_MEMBER);

        block(loginMember, taggedMember1);

        //when
        GeneralException exception = assertThrows(
                GeneralException.class,
                () -> postCustomRepository.findPostByIdWithoutBlockPost(post.getId(), loginMember.getId())
        );

        //then
        assertEquals(BLOCKED_POST, exception.getCode());
    }

    @Test
    @DisplayName("[프로필 포착 게시물 조회] 현재 로그인한 사람이 특정 포차커가 업로드한 게시물을 페이지별로 조회한다.")
    void findUploadPost() {
        //given
        Member owner = memberRepository.save(OWNER);
        Member taggedMember1 = memberRepository.save(TAGGED_MEMBER1);
        Member taggedMember2 = memberRepository.save(TAGGED_MEMBER2);
        Post post = savePost(owner, taggedMember1, taggedMember2);
        Member loginMember = memberRepository.save(LOGIN_MEMBER);

        //when
        Page<Post> posts = postCustomRepository.findUploadPost(owner, loginMember.getId(), PageRequest.of(0, 1));

        //then
        assertThat(posts.getTotalElements()).isEqualTo(1L);
        assertThat(posts.getContent().get(0)).isEqualTo(post);
    }

    @Test
    @DisplayName("[프로필 포착 게시물 조회] 현재 로그인한 사람이 게시물 작성자가 태그한 사람 중 한 명이라도 차단했을시 그 게시물은 조회되지 않는다.")
    void findUploadPostWithoutBlockPostWhenLoginMemberBlockTaggedMember() {
        //given
        Member owner = memberRepository.save(OWNER);
        Member taggedMember1 = memberRepository.save(TAGGED_MEMBER1);
        Member taggedMember2 = memberRepository.save(TAGGED_MEMBER2);
        Post post = savePost(owner, taggedMember1, taggedMember2);
        Member loginMember = memberRepository.save(LOGIN_MEMBER);

        block(loginMember, taggedMember1);

        //when
        Page<Post> posts = postCustomRepository.findUploadPost(owner, loginMember.getId(), PageRequest.of(0, 1));

        //then
        assertThat(posts.getTotalElements()).isEqualTo(0L);
        assertFalse((posts).hasContent());
    }

    @Test
    @DisplayName("[프로필 포착 게시물 조회] 현재 로그인한 사람이 게시물 작성자가 태그한 사람 중 한 명에게라도 차단당했을시 그 게시물은 조회되지 않는다.")
    void findUploadPostWithoutBlockPostWhenTaggedMemberBlockLoginMember() {
        //given
        Member owner = memberRepository.save(OWNER);
        Member taggedMember1 = memberRepository.save(TAGGED_MEMBER1);
        Member taggedMember2 = memberRepository.save(TAGGED_MEMBER2);
        Post post = savePost(owner, taggedMember1, taggedMember2);
        Member loginMember = memberRepository.save(LOGIN_MEMBER);

        block(taggedMember1, loginMember);

        //when
        Page<Post> posts = postCustomRepository.findUploadPost(owner, loginMember.getId(), PageRequest.of(0, 1));

        //then
        assertThat(posts.getTotalElements()).isEqualTo(0L);
        assertFalse((posts).hasContent());
    }

    @DisplayName("[홈탭 조회] 홈 탭이 조회된다.")
    @Test
    void findPostOfFollowing() throws Exception {
        //given
        Member owner = memberRepository.save(OWNER);
        Member taggedMember1 = memberRepository.save(TAGGED_MEMBER1);
        Member taggedMember2 = memberRepository.save(TAGGED_MEMBER2);
        Post post = savePost(owner, taggedMember1, taggedMember2);
        Member loginMember = memberRepository.save(LOGIN_MEMBER);

        //when
        Page<Post> postPage = postCustomRepository.findPostOfFollowing(
                loginMember.getId(),
                PageRequest.of(0, DEFAULT_PAGING_SIZE)
        );

        //then
        assertEquals(0, postPage.getTotalElements());
        assertEquals(0, postPage.getTotalPages());
        assertTrue(postPage.getContent().isEmpty());
    }

    @DisplayName("[홈탭 조회] 게시물을 업로더를 팔로우하면 게시물이 조회된다.")
    @Test
    void findPostOfFollowing_WhenFollowOwner() throws Exception {
        //given
        Member owner = memberRepository.save(OWNER);
        Member taggedMember1 = memberRepository.save(TAGGED_MEMBER1);
        Member taggedMember2 = memberRepository.save(TAGGED_MEMBER2);
        Post post = savePost(owner, taggedMember1, taggedMember2);
        Member loginMember = memberRepository.save(LOGIN_MEMBER);

        follow(loginMember, owner);

        //when
        Page<Post> postPage = postCustomRepository.findPostOfFollowing(
                loginMember.getId(),
                PageRequest.of(0, DEFAULT_PAGING_SIZE)
        );

        //then
        assertEquals(1, postPage.getTotalElements());
        assertEquals(1, postPage.getTotalPages());
        assertEquals(post.getId(), postPage.getContent().get(0).getId());
    }

    @DisplayName("[홈탭 조회] 게시물 태그된 사람를 팔로우하면 게시물이 조회된다.")
    @Test
    void findPostOfFollowing_WhenFollowTagged() throws Exception {
        //given
        Member owner = memberRepository.save(OWNER);
        Member taggedMember1 = memberRepository.save(TAGGED_MEMBER1);
        Member taggedMember2 = memberRepository.save(TAGGED_MEMBER2);
        Post post = savePost(owner, taggedMember1, taggedMember2);
        Member loginMember = memberRepository.save(LOGIN_MEMBER);

        follow(loginMember, taggedMember1);

        //when
        Page<Post> postPage = postCustomRepository.findPostOfFollowing(
                loginMember.getId(),
                PageRequest.of(0, DEFAULT_PAGING_SIZE)
        );

        //then
        assertEquals(1, postPage.getTotalElements());
        assertEquals(1, postPage.getTotalPages());
        assertEquals(post.getId(), postPage.getContent().get(0).getId());
    }

    @DisplayName("[홈탭 조회] 태그된 사람을 팔로우하더라도 업로더가 현재 유저를 차단하였다면 해당 게시물은 제외되어 조회된다.")
    @Test
    void findPostOfFollowing_WhenFollowTaggedAndBlockedByOwner() throws Exception {
        //given
        Member owner = memberRepository.save(OWNER);
        Member taggedMember1 = memberRepository.save(TAGGED_MEMBER1);
        Member taggedMember2 = memberRepository.save(TAGGED_MEMBER2);
        savePost(owner, taggedMember1, taggedMember2);
        Member loginMember = memberRepository.save(LOGIN_MEMBER);

        follow(loginMember, taggedMember1);
        block(owner, loginMember);

        //when
        Page<Post> postPage = postCustomRepository.findPostOfFollowing(
                loginMember.getId(),
                PageRequest.of(0, DEFAULT_PAGING_SIZE)
        );

        //then
        assertEquals(0, postPage.getTotalElements());
        assertEquals(0, postPage.getTotalPages());
        assertTrue(postPage.getContent().isEmpty());
    }

    @DisplayName("[홈탭 조회] 업로더를 팔로우하더라도 태그된 사람이 현재 유저를 차단하였다면 해당 게시물은 제외되어 조회된다.")
    @Test
    void findPostOfFollowing_WhenFollowOwnerAndBlockedByTagged() throws Exception {
        //given
        Member owner = memberRepository.save(OWNER);
        Member taggedMember1 = memberRepository.save(TAGGED_MEMBER1);
        Member taggedMember2 = memberRepository.save(TAGGED_MEMBER2);
        savePost(owner, taggedMember1, taggedMember2);
        Member loginMember = memberRepository.save(LOGIN_MEMBER);

        follow(loginMember, owner);
        block(taggedMember1, loginMember);

        //when
        Page<Post> postPage = postCustomRepository.findPostOfFollowing(
                loginMember.getId(),
                PageRequest.of(0, DEFAULT_PAGING_SIZE)
        );

        //then
        assertEquals(0, postPage.getTotalElements());
        assertEquals(0, postPage.getTotalPages());
        assertTrue(postPage.getContent().isEmpty());
    }

    @DisplayName("[홈탭 조회] 태그된 사람을 팔로우하더라도 함께 태그된 다른 사람이 현재 유저를 차단하였다면 해당 게시물은 제외되어 조회된다.")
    @Test
    void findPostOfFollowing_WhenFollowTaggedAndBlockedByTagged() throws Exception {
        //given
        Member owner = memberRepository.save(OWNER);
        Member taggedMember1 = memberRepository.save(TAGGED_MEMBER1);
        Member taggedMember2 = memberRepository.save(TAGGED_MEMBER2);
        savePost(owner, taggedMember1, taggedMember2);
        Member loginMember = memberRepository.save(LOGIN_MEMBER);

        follow(loginMember, taggedMember1);
        block(taggedMember2, loginMember);

        //when
        Page<Post> postPage = postCustomRepository.findPostOfFollowing(
                loginMember.getId(),
                PageRequest.of(0, DEFAULT_PAGING_SIZE)
        );

        //then
        assertEquals(0, postPage.getTotalElements());
        assertEquals(0, postPage.getTotalPages());
        assertTrue(postPage.getContent().isEmpty());
    }

    @DisplayName("[홈탭 조회] 태그된 사람을 팔로우하더라도 업로더를 차단하였다면 해당 게시물은 제외되어 조회된다.")
    @Test
    void findPostOfFollowing_WhenFollowTaggedAndBlockOwner() throws Exception {
        //given
        Member owner = memberRepository.save(OWNER);
        Member taggedMember1 = memberRepository.save(TAGGED_MEMBER1);
        Member taggedMember2 = memberRepository.save(TAGGED_MEMBER2);
        savePost(owner, taggedMember1, taggedMember2);
        Member loginMember = memberRepository.save(LOGIN_MEMBER);

        follow(loginMember, taggedMember1);
        block(loginMember, owner);

        //when
        Page<Post> postPage = postCustomRepository.findPostOfFollowing(
                loginMember.getId(),
                PageRequest.of(0, DEFAULT_PAGING_SIZE)
        );

        //then
        assertEquals(0, postPage.getTotalElements());
        assertEquals(0, postPage.getTotalPages());
        assertTrue(postPage.getContent().isEmpty());
    }

    @DisplayName("[홈탭 조회] 업로더를 팔로우하더라도 태그된 사람을 차단하였다면 해당 게시물은 제외되어 조회된다.")
    @Test
    void findPostOfFollowing_WhenFollowOwnerAndBlockTagged() throws Exception {
        //given
        Member owner = memberRepository.save(OWNER);
        Member taggedMember1 = memberRepository.save(TAGGED_MEMBER1);
        Member taggedMember2 = memberRepository.save(TAGGED_MEMBER2);
        savePost(owner, taggedMember1, taggedMember2);
        Member loginMember = memberRepository.save(LOGIN_MEMBER);

        follow(loginMember, owner);
        block(loginMember, taggedMember1);

        //when
        Page<Post> postPage = postCustomRepository.findPostOfFollowing(
                loginMember.getId(),
                PageRequest.of(0, DEFAULT_PAGING_SIZE)
        );

        //then
        assertEquals(0, postPage.getTotalElements());
        assertEquals(0, postPage.getTotalPages());
        assertTrue(postPage.getContent().isEmpty());
    }

    @DisplayName("[홈탭 조회] 태그된 사람을 팔로우하더라도 또 다른 태그된 사람을 차단하였다면 해당 게시물은 제외되어 조회된다.")
    @Test
    void findPostOfFollowing_WhenFollowTaggedAndBlockTagged() throws Exception {
        //given
        Member owner = memberRepository.save(OWNER);
        Member taggedMember1 = memberRepository.save(TAGGED_MEMBER1);
        Member taggedMember2 = memberRepository.save(TAGGED_MEMBER2);
        savePost(owner, taggedMember1, taggedMember2);
        Member loginMember = memberRepository.save(LOGIN_MEMBER);

        follow(loginMember, taggedMember1);
        block(loginMember, taggedMember2);

        //when
        Page<Post> postPage = postCustomRepository.findPostOfFollowing(
                loginMember.getId(),
                PageRequest.of(0, DEFAULT_PAGING_SIZE)
        );

        //then
        assertEquals(0, postPage.getTotalElements());
        assertEquals(0, postPage.getTotalPages());
        assertTrue(postPage.getContent().isEmpty());
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

    private void block(Member blocker, Member blockedMember) {
        Block block = Block.builder()
                .blocker(blocker)
                .blockedMember(blockedMember)
                .build();
        blockRepository.save(block);
    }
}