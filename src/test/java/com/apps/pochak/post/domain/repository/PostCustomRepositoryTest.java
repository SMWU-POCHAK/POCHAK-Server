package com.apps.pochak.post.domain.repository;

import com.apps.pochak.block.domain.Block;
import com.apps.pochak.block.domain.repository.BlockRepository;
import com.apps.pochak.global.TestQuerydslConfig;
import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.member.fixture.MemberFixture;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.tag.domain.Tag;
import com.apps.pochak.tag.domain.repository.TagRepository;
import lombok.Builder;
import lombok.Data;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.BLOCKED_POST;
import static com.apps.pochak.post.fixture.PostFixture.POST_WITH_MULTI_TAG;
import static com.apps.pochak.tag.fixture.TagFixture.TAG1_WITH_ONE_POST;
import static com.apps.pochak.tag.fixture.TagFixture.TAG2_WITH_ONE_POST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    PostCustomRepository postCustomRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    BlockRepository blockRepository;

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

    @DisplayName("차단된 게시물을 제외한 게시물이 조회된다.")
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

    @DisplayName("유효한 id가 없는 경우 조회되지 않는다.")
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

    @DisplayName("게시물을 업로드한 사람이 현재 로그인한 사람을 차단하였다면 조회되지 않는다.")
    @Test
    void findById_WhenOwnerBlockLoginMember() throws Exception {
        //given
        SavedPostData savedPostData = savePost();
        Member owner = savedPostData.getOwner();
        Member loginMember = savedPostData.getLoginMember();
        Post savedPost = savedPostData.getSavedPost();

        blockRepository.save(new Block(
                owner,
                loginMember
        ));

        //when
        GeneralException exception = assertThrows(
                GeneralException.class,
                () -> postCustomRepository.findPostByIdWithoutBlockPost(savedPost.getId(), loginMember.getId())
        );

        //then
        assertEquals(BLOCKED_POST, exception.getCode());
    }

    @DisplayName("게시물에 태그된 사람 중 한명이라도 현재 로그인한 사람을 차단하였다면 조회되지 않는다.")
    @Test
    void findById_WhenTaggedMemberBlockLoginMember() throws Exception {
        //given
        SavedPostData savedPostData = savePost();
        Member taggedMember1 = savedPostData.getTaggedMember1();
        Member loginMember = savedPostData.getLoginMember();
        Post savedPost = savedPostData.getSavedPost();

        blockRepository.save(new Block(
                taggedMember1,
                loginMember
        ));

        //when
        GeneralException exception = assertThrows(
                GeneralException.class,
                () -> postCustomRepository.findPostByIdWithoutBlockPost(savedPost.getId(), loginMember.getId())
        );

        //then
        assertEquals(BLOCKED_POST, exception.getCode());
    }

    @DisplayName("현재 로그인한 사람이 게시물을 업로드한 사람을 차단하였다면 조회되지 않는다.")
    @Test
    void findById_WhenLoginMemberBlockOwner() throws Exception {
        //given
        SavedPostData savedPostData = savePost();
        Member loginMember = savedPostData.getLoginMember();
        Member owner = savedPostData.getOwner();
        Post savedPost = savedPostData.getSavedPost();

        blockRepository.save(new Block(
                loginMember,
                owner
        ));

        //when
        GeneralException exception = assertThrows(
                GeneralException.class,
                () -> postCustomRepository.findPostByIdWithoutBlockPost(savedPost.getId(), loginMember.getId())
        );

        //then
        assertEquals(BLOCKED_POST, exception.getCode());
    }

    @DisplayName("현재 로그인한 사람이 게시물에 태그된 사람 중 한명이라도 차단하였다면 조회되지 않는다.")
    @Test
    void findPostByIdWithoutBlockPostWhenLoginMemberBlockTaggedMember() throws Exception {
        //given
        SavedPostData savedPostData = savePost();
        Member loginMember = savedPostData.getLoginMember();
        Member taggedMember1 = savedPostData.getTaggedMember1();
        Post savedPost = savedPostData.getSavedPost();

        blockRepository.save(new Block(
                loginMember,
                taggedMember1
        ));

        //when
        GeneralException exception = assertThrows(
                GeneralException.class,
                () -> postCustomRepository.findPostByIdWithoutBlockPost(savedPost.getId(), loginMember.getId())
        );

        //then
        assertEquals(BLOCKED_POST, exception.getCode());
    }

    @DisplayName("[홈탭 조회] 차단된 게시물을 제외한 홈 탭 게시물이 조회된다.")
    @Test
    void findPostOfFollowing() throws Exception{
        //given

        //when

        //then
    }

    @DisplayName("[홈탭 조회] 게시물을 업로드한 사람이 현재 로그인한 사람을 차단하였다면 해당 게시물은 제외되어 조회된다.")
    @Test
    void findPostOfFollowing_WhenOwnerBlockLoginMember() throws Exception{
        //given

        //when

        //then
    }

    @DisplayName("[홈탭 조회] 게시물에 태그된 사람 중 한명이라도 현재 로그인한 사람을 차단하였다면 해당 게시물은 제외되어 조회된다.")
    @Test
    void findPostOfFollowing_WhenTaggedMemberBlockLoginMember() throws Exception{
        //given

        //when

        //then
    }

    @DisplayName("[홈탭 조회] 현재 로그인한 사람이 게시물을 업로드한 사람을 차단하였다면 해당 게시물은 제외되어 조회된다.")
    @Test
    void findPostOfFollowing_WhenLoginMemberBlockOwner() throws Exception{
        //given

        //when

        //then
    }

    @DisplayName("[홈탭 조회] 현재 로그인한 사람이 게시물에 태그된 사람 중 한명이라도 차단하였다면 해당 게시물은 제외되어 조회된다.")
    @Test
    void findPostOfFollowing_WhenLoginMemberBlockTaggedMember() throws Exception{
        //given

        //when

        //then
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
        this.savedPost = savedPost;
        this.tag1 = tag1;
        this.tag2 = tag2;
        this.owner = owner;
        this.taggedMember1 = taggedMember1;
        this.taggedMember2 = taggedMember2;
        this.loginMember = loginMember;
    }
}