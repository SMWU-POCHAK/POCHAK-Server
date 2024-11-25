package com.apps.pochak.member.domain.repository;

import com.apps.pochak.block.domain.Block;
import com.apps.pochak.block.domain.repository.BlockRepository;
import com.apps.pochak.follow.domain.Follow;
import com.apps.pochak.follow.domain.repository.FollowRepository;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.dto.response.MemberElement;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static com.apps.pochak.global.Constant.DEFAULT_PAGING_SIZE;
import static com.apps.pochak.member.fixture.MemberFixture.*;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class MemberFollowCustomRepositoryTest {

    @Autowired
    MemberFollowCustomRepository memberFollowCustomRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    FollowRepository followRepository;

    @Autowired
    BlockRepository blockRepository;

    @AfterEach
    void deleteAll() {
        memberRepository.deleteAll();
        followRepository.deleteAll();
        blockRepository.deleteAll();
    }

    @DisplayName("[팔로워 조회] 팔로워가 정상적으로 조회된다.")
    @Test
    void findFollowers() throws Exception {
        // given
        Member member1 = memberRepository.save(TAGGED_MEMBER1);
        Member member2 = memberRepository.save(TAGGED_MEMBER2);
        Member receiver = memberRepository.save(OWNER);
        Member loginMember = memberRepository.save(LOGIN_MEMBER);

        follow(member1, receiver);
        follow(member2, receiver);
        follow(loginMember, receiver);
        follow(loginMember, member2);

        // when
        PageRequest pageRequest = PageRequest.of(0, DEFAULT_PAGING_SIZE);
        Page<MemberElement> memberElementPage = memberFollowCustomRepository
                .findFollowersOfMemberAndIsFollow(
                        receiver.getId(),
                        loginMember.getId(),
                        pageRequest
                );

        // then
        assertAll(
                () -> assertEquals(3L, memberElementPage.getContent().size()),
                () -> assertEquals(3L, memberElementPage.getTotalElements()),
                () -> assertEquals(1L, memberElementPage.getTotalPages()),
                // order desc
                () -> assertEquals(loginMember.getId(), memberElementPage.getContent().get(0).getMemberId()),
                () -> assertNull(memberElementPage.getContent().get(0).getIsFollow()),
                () -> assertEquals(member2.getId(), memberElementPage.getContent().get(1).getMemberId()),
                () -> assertTrue(memberElementPage.getContent().get(1).getIsFollow()),
                () -> assertEquals(member1.getId(), memberElementPage.getContent().get(2).getMemberId()),
                () -> assertFalse(memberElementPage.getContent().get(2).getIsFollow())
        );
    }

    @DisplayName("[팔로워 조회] 팔로워가 현재 유저를 차단하였다면 해당 유저는 제외되어 조회된다.")
    @Test
    void findFollowers_WhenLoginMemberBlockFollower() throws Exception {
        // given
        Member member1 = memberRepository.save(TAGGED_MEMBER1);
        Member member2 = memberRepository.save(TAGGED_MEMBER2);
        Member receiver = memberRepository.save(OWNER);
        Member loginMember = memberRepository.save(LOGIN_MEMBER);

        follow(member1, receiver);
        follow(member2, receiver);

        block(member1, loginMember);

        // when
        PageRequest pageRequest = PageRequest.of(0, DEFAULT_PAGING_SIZE);
        Page<MemberElement> memberElementPage = memberFollowCustomRepository
                .findFollowersOfMemberAndIsFollow(
                        receiver.getId(),
                        loginMember.getId(),
                        pageRequest
                );

        // then
        assertAll(
                () -> assertEquals(1L, memberElementPage.getContent().size()),
                () -> assertEquals(1L, memberElementPage.getTotalElements()),
                () -> assertEquals(1L, memberElementPage.getTotalPages()),
                () -> assertEquals(member2.getId(), memberElementPage.getContent().get(0).getMemberId()),
                () -> assertFalse(memberElementPage.getContent().get(0).getIsFollow())
        );
    }

    @DisplayName("[팔로워 조회] 현재 유저가 팔로워를 차단하였다면 해당 유저는 제외되어 조회된다.")
    @Test
    void findFollowers_WhenFollowerBlockLoginMember() throws Exception {
        // given
        Member member1 = memberRepository.save(TAGGED_MEMBER1);
        Member member2 = memberRepository.save(TAGGED_MEMBER2);
        Member receiver = memberRepository.save(OWNER);
        Member loginMember = memberRepository.save(LOGIN_MEMBER);

        follow(member1, receiver);
        follow(member2, receiver);

        block(loginMember, member1);

        // when
        PageRequest pageRequest = PageRequest.of(0, DEFAULT_PAGING_SIZE);
        Page<MemberElement> memberElementPage = memberFollowCustomRepository
                .findFollowersOfMemberAndIsFollow(
                        receiver.getId(),
                        loginMember.getId(),
                        pageRequest
                );

        // then
        assertAll(
                () -> assertEquals(1L, memberElementPage.getContent().size()),
                () -> assertEquals(1L, memberElementPage.getTotalElements()),
                () -> assertEquals(1L, memberElementPage.getTotalPages()),
                () -> assertEquals(member2.getId(), memberElementPage.getContent().get(0).getMemberId()),
                () -> assertFalse(memberElementPage.getContent().get(0).getIsFollow())
        );
    }

    @DisplayName("[팔로잉 조회] 팔로잉이 정상적으로 조회된다.")
    @Test
    void findFollowings() throws Exception {
        // given
        Member member1 = memberRepository.save(TAGGED_MEMBER1);
        Member member2 = memberRepository.save(TAGGED_MEMBER2);
        Member sender = memberRepository.save(OWNER);
        Member loginMember = memberRepository.save(LOGIN_MEMBER);

        follow(sender, member1);
        follow(sender, member2);
        follow(sender, loginMember);
        follow(loginMember, member2);

        // when
        PageRequest pageRequest = PageRequest.of(0, DEFAULT_PAGING_SIZE);
        Page<MemberElement> memberElementPage = memberFollowCustomRepository
                .findFollowingsOfMemberAndIsFollow(
                        sender.getId(),
                        loginMember.getId(),
                        pageRequest
                );

        // then
        assertAll(
                () -> assertEquals(3L, memberElementPage.getContent().size()),
                () -> assertEquals(3L, memberElementPage.getTotalElements()),
                () -> assertEquals(1L, memberElementPage.getTotalPages()),
                // order desc
                () -> assertEquals(loginMember.getId(), memberElementPage.getContent().get(0).getMemberId()),
                () -> assertNull(memberElementPage.getContent().get(0).getIsFollow()),
                () -> assertEquals(member2.getId(), memberElementPage.getContent().get(1).getMemberId()),
                () -> assertTrue(memberElementPage.getContent().get(1).getIsFollow()),
                () -> assertEquals(member1.getId(), memberElementPage.getContent().get(2).getMemberId()),
                () -> assertFalse(memberElementPage.getContent().get(2).getIsFollow())
        );
    }

    @DisplayName("[팔로잉 조회] 팔로잉 유저가 현재 유저를 차단하였다면, 해당 유저는 제외되어 조회된다.")
    @Test
    void findFollowings_WhenFollowingBlockLoginMember() throws Exception{
        // given
        Member member1 = memberRepository.save(TAGGED_MEMBER1);
        Member member2 = memberRepository.save(TAGGED_MEMBER2);
        Member sender = memberRepository.save(OWNER);
        Member loginMember = memberRepository.save(LOGIN_MEMBER);

        follow(sender, member1);
        follow(sender, member2);

        block(member1, loginMember);

        // when
        PageRequest pageRequest = PageRequest.of(0, DEFAULT_PAGING_SIZE);
        Page<MemberElement> memberElementPage = memberFollowCustomRepository
                .findFollowingsOfMemberAndIsFollow(
                        sender.getId(),
                        loginMember.getId(),
                        pageRequest
                );

        // then
        assertAll(
                () -> assertEquals(1L, memberElementPage.getContent().size()),
                () -> assertEquals(1L, memberElementPage.getTotalElements()),
                () -> assertEquals(1L, memberElementPage.getTotalPages()),
                () -> assertEquals(member2.getId(), memberElementPage.getContent().get(0).getMemberId()),
                () -> assertFalse(memberElementPage.getContent().get(0).getIsFollow())
        );
    }

    @DisplayName("[팔로잉 조회] 현재 유저가 팔로잉을 차단하였다면 해당 유저는 제외되어 조회된다.")
    @Test
    void findFollowings_WhenLoginMemberBlockFollowing() throws Exception{
        // given
        Member member1 = memberRepository.save(TAGGED_MEMBER1);
        Member member2 = memberRepository.save(TAGGED_MEMBER2);
        Member sender = memberRepository.save(OWNER);
        Member loginMember = memberRepository.save(LOGIN_MEMBER);

        follow(sender, member1);
        follow(sender, member2);

        block(loginMember, member1);

        // when
        PageRequest pageRequest = PageRequest.of(0, DEFAULT_PAGING_SIZE);
        Page<MemberElement> memberElementPage = memberFollowCustomRepository
                .findFollowingsOfMemberAndIsFollow(
                        sender.getId(),
                        loginMember.getId(),
                        pageRequest
                );

        // then
        assertAll(
                () -> assertEquals(1L, memberElementPage.getContent().size()),
                () -> assertEquals(1L, memberElementPage.getTotalElements()),
                () -> assertEquals(1L, memberElementPage.getTotalPages()),
                () -> assertEquals(member2.getId(), memberElementPage.getContent().get(0).getMemberId()),
                () -> assertFalse(memberElementPage.getContent().get(0).getIsFollow())
        );
    }

    private void follow(
            final Member sender,
            final Member receiver
    ) {
        Follow follow = Follow.of()
                .sender(sender)
                .receiver(receiver)
                .build();
        followRepository.save(follow);
    }

    private void block(
            final Member blocker,
            final Member blockedMember
    ) {
        Block block = Block.builder()
                .blocker(blocker)
                .blockedMember(blockedMember)
                .build();
        blockRepository.save(block);
    }
}