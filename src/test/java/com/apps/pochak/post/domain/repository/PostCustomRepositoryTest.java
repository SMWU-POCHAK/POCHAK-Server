package com.apps.pochak.post.domain.repository;

import com.apps.pochak.block.domain.Block;
import com.apps.pochak.block.domain.repository.BlockRepository;
import com.apps.pochak.follow.domain.Follow;
import com.apps.pochak.follow.domain.repository.FollowRepository;
import com.apps.pochak.global.BaseEntityStatus;
import com.apps.pochak.global.TestQuerydslConfig;
import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.member.fixture.MemberFixture;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.tag.domain.Tag;
import com.apps.pochak.tag.domain.repository.TagRepository;
import jakarta.persistence.EntityManager;
import lombok.Builder;
import lombok.Data;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static com.apps.pochak.global.Constant.DEFAULT_PAGING_SIZE;
import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.BLOCKED_POST;
import static com.apps.pochak.post.fixture.PostFixture.POST_WITH_MULTI_TAG;
import static com.apps.pochak.tag.fixture.TagFixture.TAG1_WITH_ONE_POST;
import static com.apps.pochak.tag.fixture.TagFixture.TAG2_WITH_ONE_POST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(TestQuerydslConfig.class)
class PostCustomRepositoryTest {

    private static final Post POST = POST_WITH_MULTI_TAG;
    private static final Tag TAG1 = TAG1_WITH_ONE_POST;
    private static final Tag TAG2 = TAG2_WITH_ONE_POST;
    private static final Member OWNER = MemberFixture.OWNER;
    private static final Member TAGGED_MEMBER1 = MemberFixture.TAGGED_MEMBER1;
    private static final Member TAGGED_MEMBER2 = MemberFixture.TAGGED_MEMBER2;
    private static final Member LOGIN_MEMBER = MemberFixture.LOGIN_MEMBER;

    @Autowired
    private EntityManager em;

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


    @DisplayName("[게시물 id 조회] 차단된 게시물을 제외한 게시물이 조회된다.")
    @Test
    void findById() throws Exception {
        //given
        SavedPostData savedPostData = savePost();
        Member loginMember = savedPostData.getLoginMember();
        Post savedPost = savedPostData.getSavedPost();

        //when
        Post findPost = postCustomRepository.findPostByIdWithoutBlockPost(savedPost.getId(), loginMember.getId());

        //then
        assertEquals(savedPost.getId(), findPost.getId());
    }

    @DisplayName("[게시물 id 조회] 유효한 id가 없는 경우 조회되지 않는다.")
    @Test
    void findById_WhenIdIsInvalid() throws Exception {
        //given
        SavedPostData savedPostData = savePost();
        Member loginMember = savedPostData.getLoginMember();
        Long invalidPostId = 0L;

        //when
        GeneralException exception = assertThrows(
                GeneralException.class,
                () -> postCustomRepository.findPostByIdWithoutBlockPost(invalidPostId, loginMember.getId())
        );

        //then
        assertEquals(BLOCKED_POST, exception.getCode());
    }

    @DisplayName("[게시물 id 조회] 업로더가 현재 유저를 차단하였다면 조회되지 않는다.")
    @Test
    void findById_WhenOwnerBlockLoginMember() throws Exception {
        //given
        SavedPostData savedPostData = savePost();
        Member loginMember = savedPostData.getLoginMember();
        Post savedPost = savedPostData.getSavedPost();

        block(savedPostData.getOwner(), loginMember);

        //when
        GeneralException exception = assertThrows(
                GeneralException.class,
                () -> postCustomRepository.findPostByIdWithoutBlockPost(savedPost.getId(), loginMember.getId())
        );

        //then
        assertEquals(BLOCKED_POST, exception.getCode());
    }

    @DisplayName("[게시물 id 조회] 태그된 사람이 현재 유저를 차단하였다면 조회되지 않는다.")
    @Test
    void findById_WhenTaggedMemberBlockLoginMember() throws Exception {
        //given
        SavedPostData savedPostData = savePost();
        Member taggedMember1 = savedPostData.getTaggedMember1();
        Member loginMember = savedPostData.getLoginMember();
        Post savedPost = savedPostData.getSavedPost();

        block(taggedMember1, loginMember);

        //when
        GeneralException exception = assertThrows(
                GeneralException.class,
                () -> postCustomRepository.findPostByIdWithoutBlockPost(savedPost.getId(), loginMember.getId())
        );

        //then
        assertEquals(BLOCKED_POST, exception.getCode());
    }

    @DisplayName("[게시물 id 조회] 현재 유저가 게시물 업로더를 차단하였다면 조회되지 않는다.")
    @Test
    void findById_WhenLoginMemberBlockOwner() throws Exception {
        //given
        SavedPostData savedPostData = savePost();
        Member loginMember = savedPostData.getLoginMember();
        Post savedPost = savedPostData.getSavedPost();

        block(loginMember, savedPostData.getOwner());

        //when
        GeneralException exception = assertThrows(
                GeneralException.class,
                () -> postCustomRepository.findPostByIdWithoutBlockPost(savedPost.getId(), loginMember.getId())
        );

        //then
        assertEquals(BLOCKED_POST, exception.getCode());
    }

    @DisplayName("[게시물 id 조회] 현재 유저가 게시물 업로더 중 한명이라도 차단하였다면 조회되지 않는다.")
    @Test
    void findPostById_WhenLoginMemberBlockTaggedMember() throws Exception {
        //given
        SavedPostData savedPostData = savePost();
        Member loginMember = savedPostData.getLoginMember();
        Post savedPost = savedPostData.getSavedPost();

        block(loginMember, savedPostData.getTaggedMember1());

        //when
        GeneralException exception = assertThrows(
                GeneralException.class,
                () -> postCustomRepository.findPostByIdWithoutBlockPost(savedPost.getId(), loginMember.getId())
        );

        //then
        assertEquals(BLOCKED_POST, exception.getCode());
    }

    @Test
    @DisplayName("[프로필 포착 게시물 조회] 현재 로그인한 사람이 특정 포차커가 업로드한 게시물을 페이지별로 조회한다.")
    void findUploadPostPage() {
        //given
        SavedPostData savedPostData = savePost();
        Member owner = savedPostData.getOwner();
        Member loginMember = savedPostData.getLoginMember();
        savedPostData.getSavedPost().makePublic();
        savedPostData.getSavedPost().setStatus(BaseEntityStatus.ACTIVE);

        //when
        Page<Post> posts = postCustomRepository.findUploadPostPage(owner, loginMember.getId(), PageRequest.of(0, 1));

        //then
        assertThat(posts.getTotalElements()).isEqualTo(1L);
        assertThat(posts.getContent().get(0)).isEqualTo(savedPostData.getSavedPost());
    }

    @Test
    @DisplayName("[프로필 포착 게시물 조회] 현재 로그인한 사람이 게시물 작성자가 태그한 사람 중 한 명이라도 차단했을시 그 게시물은 조회되지 않는다.")
    void findUploadPostPageWithoutBlockPostWhenLoginMemberBlockTaggedMember() {
        //given
        SavedPostData savedPostData = savePost();
        Member owner = savedPostData.getOwner();
        Member loginMember = savedPostData.getLoginMember();
        savedPostData.getSavedPost().makePublic();
        savedPostData.getSavedPost().setStatus(BaseEntityStatus.ACTIVE);

        block(loginMember, savedPostData.getTaggedMember1());

        //when
        Page<Post> posts = postCustomRepository.findUploadPostPage(owner, loginMember.getId(), PageRequest.of(0, 1));
        //then
        assertThat(posts.getTotalElements()).isEqualTo(0L);
        assertFalse((posts).hasContent());
    }

    @Test
    @DisplayName("[프로필 포착 게시물 조회] 현재 로그인한 사람이 게시물 작성자가 태그한 사람 중 한 명에게라도 차단당했을시 그 게시물은 조회되지 않는다.")
    void findUploadPostPageWithoutBlockPostWhenTaggedMemberBlockLoginMember() {
        //given
        SavedPostData savedPostData = savePost();
        Member owner = savedPostData.getOwner();
        Member loginMember = savedPostData.getLoginMember();
        savedPostData.getSavedPost().makePublic();
        savedPostData.getSavedPost().setStatus(BaseEntityStatus.ACTIVE);

        block(savedPostData.getTaggedMember1(), loginMember);

        //when
        Page<Post> posts = postCustomRepository.findUploadPostPage(owner, loginMember.getId(), PageRequest.of(0, 1));
        //then
        assertThat(posts.getTotalElements()).isEqualTo(0L);
        assertFalse((posts).hasContent());
    }

    @DisplayName("[홈탭 조회] 홈 탭이 조회된다.")
    @Test
    void findPostOfFollowing() throws Exception {
        //given
        SavedPostData savedPostData = savePost();
        Member loginMember = savedPostData.getLoginMember();

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
        SavedPostData savedPostData = savePost();
        Post savedPost = savedPostData.getSavedPost();
        Member loginMember = savedPostData.getLoginMember();

        follow(loginMember, savedPost.getOwner());

        //when
        Page<Post> postPage = postCustomRepository.findPostOfFollowing(
                loginMember.getId(),
                PageRequest.of(0, DEFAULT_PAGING_SIZE)
        );

        //then
        assertEquals(1, postPage.getTotalElements());
        assertEquals(1, postPage.getTotalPages());
        assertEquals(savedPost.getId(), postPage.getContent().get(0).getId());
    }

    @DisplayName("[홈탭 조회] 게시물 태그된 사람를 팔로우하면 게시물이 조회된다.")
    @Test
    void findPostOfFollowing_WhenFollowTagged() throws Exception {
        //given
        SavedPostData savedPostData = savePost();
        Post savedPost = savedPostData.getSavedPost();
        Member loginMember = savedPostData.getLoginMember();

        follow(loginMember, savedPostData.getTaggedMember1());

        //when
        Page<Post> postPage = postCustomRepository.findPostOfFollowing(
                loginMember.getId(),
                PageRequest.of(0, DEFAULT_PAGING_SIZE)
        );

        //then
        assertEquals(1, postPage.getTotalElements());
        assertEquals(1, postPage.getTotalPages());
        assertEquals(savedPost.getId(), postPage.getContent().get(0).getId());
    }

    @DisplayName("[홈탭 조회] 태그된 사람을 팔로우하더라도 업로더가 현재 유저를 차단하였다면 해당 게시물은 제외되어 조회된다.")
    @Test
    void findPostOfFollowing_WhenFollowTaggedAndBlockedByOwner() throws Exception {
        //given
        SavedPostData savedPostData = savePost();
        Member loginMember = savedPostData.getLoginMember();

        follow(loginMember, savedPostData.getTaggedMember1());
        block(savedPostData.getOwner(), loginMember);

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
        SavedPostData savedPostData = savePost();
        Member loginMember = savedPostData.getLoginMember();

        follow(loginMember, savedPostData.getOwner());
        block(savedPostData.getTaggedMember1(), loginMember);

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
        SavedPostData savedPostData = savePost();
        Member loginMember = savedPostData.getLoginMember();

        follow(loginMember, savedPostData.getTaggedMember1());
        block(savedPostData.getTaggedMember2(), loginMember);

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
        SavedPostData savedPostData = savePost();
        Member loginMember = savedPostData.getLoginMember();

        follow(loginMember, savedPostData.getTaggedMember1());
        block(loginMember, savedPostData.getOwner());

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
        SavedPostData savedPostData = savePost();
        Member loginMember = savedPostData.getLoginMember();

        follow(loginMember, savedPostData.getOwner());
        block(loginMember, savedPostData.getTaggedMember1());

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
        SavedPostData savedPostData = savePost();
        Member loginMember = savedPostData.getLoginMember();

        follow(loginMember, savedPostData.getTaggedMember1());
        block(loginMember, savedPostData.getTaggedMember2());

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

    @DisplayName("[프로필 POCHAKED 탭 조회] 차단 없이 태그된 게시물이 정상적으로 조회된다.")
    @Test
    void findTaggedPost_WhenNoBlock() throws Exception {
        //given
        SavedPostData savedPostData = savePost();
        Member loginMember = savedPostData.getLoginMember();

        //when
        Page<Post> postPage = postCustomRepository.findTaggedPostPage(
                savedPostData.getTaggedMember1(),
                loginMember.getId(),
                PageRequest.of(0, DEFAULT_PAGING_SIZE)
        );

        //then
        assertEquals(1, postPage.getTotalElements());
        assertEquals(1, postPage.getTotalPages());
        assertFalse(postPage.getContent().isEmpty());
        assertEquals(savedPostData.getSavedPost().getId(), postPage.getContent().get(0).getId());
    }


    @DisplayName("[프로필 POCHAKED 탭 조회] 게시글의 주인에게 차단 당하였다면 해당 게시물은 제외되어 조회된다. ")
    @Test
    void findTaggedPost_WhenBlockedByPostOwner() throws Exception {
        //given
        SavedPostData savedPostData = savePost();
        Member loginMember = savedPostData.getLoginMember();
        block(savedPostData.getOwner(), loginMember);

        //when
        Page<Post> postPage = postCustomRepository.findTaggedPostPage(
                savedPostData.getTaggedMember1(),
                loginMember.getId(),
                PageRequest.of(0, DEFAULT_PAGING_SIZE)
        );

        //then
        assertEquals(0, postPage.getTotalElements());
        assertEquals(0, postPage.getTotalPages());
        assertTrue(postPage.getContent().isEmpty());
    }

    @DisplayName("[프로필 POCHAKED 탭 조회] 게시글에 태그된 멤버에게 차단 당하였다면 해당 게시물은 제외되어 조회된다. ")
    @Test
    void findTaggedPost_WhenBlockedByTaggedMember() throws Exception {
        //given
        SavedPostData savedPostData = savePost();
        Member loginMember = savedPostData.getLoginMember();
        block(savedPostData.getTaggedMember1(), loginMember);

        //when
        Page<Post> postPage = postCustomRepository.findTaggedPostPage(
                savedPostData.getTaggedMember1(),
                loginMember.getId(),
                PageRequest.of(0, DEFAULT_PAGING_SIZE)
        );

        //then
        assertEquals(0, postPage.getTotalElements());
        assertEquals(0, postPage.getTotalPages());
        assertTrue(postPage.getContent().isEmpty());
    }

    @DisplayName("[프로필 POCHAKED 탭 조회] 게시글의 주인을 차단하였다면 해당 게시물은 제외되어 조회된다. ")
    @Test
    void findTaggedPost_WhenBlockingPostOwner() throws Exception {
        //given
        SavedPostData savedPostData = savePost();
        Member loginMember = savedPostData.getLoginMember();
        block(loginMember, savedPostData.getOwner());

        //when
        Page<Post> postPage = postCustomRepository.findTaggedPostPage(
                savedPostData.getTaggedMember1(),
                loginMember.getId(),
                PageRequest.of(0, DEFAULT_PAGING_SIZE)
        );

        //then
        assertEquals(0, postPage.getTotalElements());
        assertEquals(0, postPage.getTotalPages());
        assertTrue(postPage.getContent().isEmpty());
    }

    @DisplayName("[프로필 POCHAKED 탭 조회] 게시글에 태그된 멤버를 차단하였다면 해당 게시물은 제외되어 조회된다. ")
    @Test
    void findTaggedPost_WhenBlockingTaggedMember() throws Exception {
        //given
        SavedPostData savedPostData = savePost();
        Member loginMember = savedPostData.getLoginMember();
        block(loginMember, savedPostData.getTaggedMember1());

        //when
        Page<Post> postPage = postCustomRepository.findTaggedPostPage(
                savedPostData.getTaggedMember1(),
                loginMember.getId(),
                PageRequest.of(0, DEFAULT_PAGING_SIZE)
        );

        //then
        assertEquals(0, postPage.getTotalElements());
        assertEquals(0, postPage.getTotalPages());
        assertTrue(postPage.getContent().isEmpty());
    }


    private SavedPostData savePost() {
        return SavedPostData.of()
                .owner(memberRepository.save(OWNER))
                .taggedMember1(memberRepository.save(TAGGED_MEMBER1))
                .taggedMember2(memberRepository.save(TAGGED_MEMBER2))
                .loginMember(memberRepository.save(LOGIN_MEMBER))
                .savedPost(postRepository.save(POST))
                .tag1(tagRepository.save(TAG1))
                .tag2(tagRepository.save(TAG2))
                .build();
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

@Data
class SavedPostData {
    private Post savedPost;
    private Tag tag1;
    private Tag tag2;
    private Member owner;
    private Member taggedMember1;
    private Member taggedMember2;
    private Member loginMember;

    @Builder(builderMethodName = "of")
    public SavedPostData(
            final Post savedPost,
            final Tag tag1,
            final Tag tag2,
            final Member owner,
            final Member taggedMember1,
            final Member taggedMember2,
            final Member loginMember
    ) {
        savedPost.makePublic();
        this.savedPost = savedPost;
        this.tag1 = tag1;
        this.tag2 = tag2;
        this.owner = owner;
        this.taggedMember1 = taggedMember1;
        this.taggedMember2 = taggedMember2;
        this.loginMember = loginMember;
    }
}