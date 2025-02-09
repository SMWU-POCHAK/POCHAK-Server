package com.apps.pochak.block.service;

import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.block.domain.Block;
import com.apps.pochak.block.domain.repository.BlockRepository;
import com.apps.pochak.block.dto.response.BlockElement;
import com.apps.pochak.block.dto.response.BlockElements;
import com.apps.pochak.follow.domain.Follow;
import com.apps.pochak.follow.domain.repository.FollowRepository;
import com.apps.pochak.global.ServiceTest;
import com.apps.pochak.like.domain.LikeEntity;
import com.apps.pochak.like.domain.repository.LikeRepository;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
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

import java.util.List;

import static com.apps.pochak.global.BaseEntityStatus.*;
import static com.apps.pochak.global.Constant.DEFAULT_PAGING_SIZE;
import static com.apps.pochak.member.fixture.MemberFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class BlockServiceTest extends ServiceTest {

    @Autowired
    BlockService blockService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    FollowRepository followRepository;

    @Autowired
    LikeRepository likeRepository;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    BlockRepository blockRepository;

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
        follow(owner, taggedMember1);
        Post post = savePublicPost(owner, taggedMember1, taggedMember2);
        like(taggedMember1, post);
        like(owner, post);

        // when
        blockService.blockMember(
                Accessor.member(taggedMember1.getId()),
                owner.getHandle()
        );

        // then
        List<Follow> followList = List.of(
                followRepository.findBySenderAndReceiver(taggedMember1, owner),
                followRepository.findBySenderAndReceiver(owner, taggedMember1)
        );
        List<LikeEntity> likeList = List.of(
                likeRepository.findByMemberAndPost(taggedMember1, post).get(),
                likeRepository.findByMemberAndPost(owner, post).get()
        );
        Post findPost = postRepository.findById(post.getId()).get();

        assertAll(
                () -> assertThat(followList)
                        .extracting(Follow::getStatus)
                        .containsOnly(DELETED),
                () -> assertThat(likeList)
                        .extracting(LikeEntity::getStatus)
                        .containsOnly(DELETED),
                () -> assertEquals(INACTIVE, findPost.getStatus())
        );
    }

    @DisplayName("차단한 멤버를 조회한다.")
    @Test
    void getBlockedMember() throws Exception {
        // given
        block(loginMember, taggedMember1);
        block(loginMember, taggedMember2);

        // when
        BlockElements blockElements = blockService.getBlockedMember(
                Accessor.member(loginMember.getId()),
                loginMember.getHandle(),
                PageRequest.of(0, DEFAULT_PAGING_SIZE)
        );

        // then
        assertThat(blockElements.getBlockList())
                .extracting(BlockElement::getMemberId)
                .containsExactly(taggedMember1.getId(), taggedMember2.getId());
    }

    @DisplayName("차단 해제 시, 비활성화 처리됐던 게시물이 활성화된다.")
    @Test
    void cancelBlock() throws Exception {
        // given
        Post post = savePublicPost(owner, taggedMember1, taggedMember2);
        blockService.blockMember(
                Accessor.member(taggedMember1.getId()),
                owner.getHandle()
        );

        // when
        blockService.cancelBlock(
                Accessor.member(taggedMember1.getId()),
                taggedMember1.getHandle(),
                owner.getHandle()
        );

        // then
        Post findPost = postRepository.findById(post.getId()).get();
        assertEquals(ACTIVE, findPost.getStatus());
    }

    @DisplayName("차단 해제 시, 다른 멤버를 사이에 차단 상태가 남아있다면 게시물이 활성화되지 않는다.")
    @Test
    void cancelBlock_WhenBlockRemainsBetweenOwnerAndTaggedMember() throws Exception {
        // given
        Post post = savePublicPost(owner, taggedMember1, taggedMember2);
        blockService.blockMember(
                Accessor.member(taggedMember1.getId()),
                owner.getHandle()
        );
        blockService.blockMember(
                Accessor.member(owner.getId()),
                taggedMember2.getHandle()
        );

        // when
        blockService.cancelBlock(
                Accessor.member(taggedMember1.getId()),
                taggedMember1.getHandle(),
                owner.getHandle()
        );

        // then
        Post findPost = postRepository.findById(post.getId()).get();
        assertEquals(INACTIVE, findPost.getStatus());
    }

    @DisplayName("차단 해제 시, 다른 멤버를 사이에 차단 상태가 남아있다면 게시물이 활성화되지 않는다.")
    @Test
    void cancelBlock_WhenBlockRemainsBetweenTaggedMembers() throws Exception {
        // given
        Post post = savePublicPost(owner, taggedMember1, taggedMember2);
        blockService.blockMember(
                Accessor.member(taggedMember1.getId()),
                owner.getHandle()
        );
        blockService.blockMember(
                Accessor.member(taggedMember2.getId()),
                taggedMember1.getHandle()
        );

        // when
        blockService.cancelBlock(
                Accessor.member(taggedMember1.getId()),
                taggedMember1.getHandle(),
                owner.getHandle()
        );

        // then
        Post findPost = postRepository.findById(post.getId()).get();
        assertEquals(INACTIVE, findPost.getStatus());
    }

    private Post savePublicPost(Member owner, Member... taggedMemberList) {
        Post post = postRepository.save(PostFixture.get(owner));
        saveTags(post, taggedMemberList);
        post.makePublic();
        return post;
    }

    private void saveTags(Post post, Member... tagMemberList) {
        for (Member member : tagMemberList) {
            tagRepository.save(new Tag(post, member));
        }
    }

    private Follow follow(Member sender, Member receiver) {
        return followRepository.save(new Follow(sender, receiver));
    }

    private LikeEntity like(Member member, Post post) {
        return likeRepository.save(new LikeEntity(member, post));
    }

    private Block block(Member blocker, Member blockedMember) {
        return blockRepository.save(new Block(blocker, blockedMember));
    }
}