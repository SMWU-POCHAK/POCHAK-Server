package com.apps.pochak.follow.service;

import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.block.domain.Block;
import com.apps.pochak.block.domain.repository.BlockRepository;
import com.apps.pochak.follow.domain.Follow;
import com.apps.pochak.follow.domain.repository.FollowRepository;
import com.apps.pochak.global.ServiceTest;
import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.member.dto.response.MemberElement;
import com.apps.pochak.member.dto.response.MemberElements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.apps.pochak.global.BaseEntityStatus.ACTIVE;
import static com.apps.pochak.global.BaseEntityStatus.DELETED;
import static com.apps.pochak.global.Constant.DEFAULT_PAGING_SIZE;
import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.NOT_FOLLOW;
import static com.apps.pochak.member.fixture.MemberFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class FollowServiceTest extends ServiceTest {

    @Autowired
    FollowService followService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    FollowRepository followRepository;

    @Autowired
    BlockRepository blockRepository;

    private Member sender;
    private Member receiver;
    private Member loginMember;
    private Member member;


    @BeforeEach
    void setUp() {
        sender = memberRepository.save(TAGGED_MEMBER1);
        receiver = memberRepository.save(TAGGED_MEMBER2);
        loginMember = memberRepository.save(LOGIN_MEMBER);
        member = memberRepository.save(MEMBER);
    }

    @DisplayName("[팔로우] 팔로우가 정상적으로 저장된다.")
    @Test
    void follow() throws Exception {
        // when
        followService.follow(
                Accessor.member(sender.getId()),
                receiver.getHandle()
        );

        // then
        List<Follow> followList = followRepository.findAll();
        Follow follow = followList.stream()
                .filter(f -> f.getSender().equals(sender) && f.getReceiver().equals(receiver))
                .findFirst()
                .orElseThrow(() -> new AssertionError("팔로우 저장 실패"));

        assertAll(
                () -> assertEquals(1, followList.size()),
                () -> assertEquals(ACTIVE, follow.getStatus())
        );
    }

    @DisplayName("[팔로우] 팔로우가 정상적으로 취소된다.")
    @Test
    void cancelFollow() throws Exception {
        // when
        followService.follow(
                Accessor.member(sender.getId()),
                receiver.getHandle()
        );
        followService.follow(
                Accessor.member(sender.getId()),
                receiver.getHandle()
        );

        // then
        List<Follow> followList = followRepository.findAll();
        Follow follow = followList.stream()
                .filter(f -> f.getSender().equals(sender) && f.getReceiver().equals(receiver))
                .findFirst()
                .orElseThrow(() -> new AssertionError("팔로우 저장 실패"));

        assertAll(
                () -> assertEquals(1, followList.size()),
                () -> assertEquals(DELETED, follow.getStatus())
        );
    }

    @DisplayName("[팔로워 삭제] 팔로워가 정상적으로 삭제된다.")
    @Test
    void deleteFollower() throws Exception {
        // given
        follow(sender, receiver);

        // when
        followService.deleteFollower(
                Accessor.member(receiver.getId()),
                receiver.getHandle(),
                sender.getHandle()
        );

        // then
        List<Follow> followList = followRepository.findAll();
        Follow follow = followList.stream()
                .filter(f -> f.getSender().equals(sender) && f.getReceiver().equals(receiver))
                .findFirst()
                .orElseThrow(() -> new AssertionError("팔로우 저장 실패"));

        assertAll(
                () -> assertEquals(1, followList.size()),
                () -> assertEquals(DELETED, follow.getStatus())
        );
    }

    @DisplayName("[팔로워 삭제] 팔로워가 아닌 사람을 요청하면 예외가 발생한다.")
    @Test
    void deleteFollower_whenNotFollower() throws Exception {
        // given
        follow(sender, receiver);

        // when, then
        GeneralException exception = assertThrows(
                GeneralException.class,
                () -> followService.deleteFollower(
                        Accessor.member(receiver.getId()),
                        receiver.getHandle(),
                        loginMember.getHandle()
                )
        );

        assertEquals(NOT_FOLLOW, exception.getCode());
    }

    @DisplayName("[팔로잉 조회] 유저가 팔로우하고 있는 유저들이 조회된다.")
    @Test
    void getFollowings() throws Exception {
        // given
        follow(sender, receiver);
        follow(sender, member);
        follow(loginMember, receiver);

        // when
        MemberElements memberElements = followService.getFollowings(
                Accessor.member(loginMember.getId()),
                sender.getHandle(),
                PageRequest.of(0, DEFAULT_PAGING_SIZE)
        );

        // then
        List<MemberElement> memberElementList = memberElements.getMemberList();
        assertAll(
                () -> assertThat(memberElementList)
                        .extracting(MemberElement::getMemberId)
                        .containsExactly(member.getId(), receiver.getId()),
                () -> assertThat(memberElementList)
                        .extracting(MemberElement::getIsFollow)
                        .containsExactly(false, true)
        );
    }

    @DisplayName("[팔로잉 조회] 현재 유저를 차단하였을 경우 해당 유저는 제외되어 조회된다.")
    @Test
    void getFollowings_whenBlocked() throws Exception {
        // given
        follow(sender, receiver);
        follow(sender, member);
        block(receiver, loginMember);

        // when
        MemberElements memberElements = followService.getFollowings(
                Accessor.member(loginMember.getId()),
                sender.getHandle(),
                PageRequest.of(0, DEFAULT_PAGING_SIZE)
        );

        // then
        List<MemberElement> memberElementList = memberElements.getMemberList();
        assertAll(
                () -> assertThat(memberElementList)
                        .extracting(MemberElement::getMemberId)
                        .containsExactly(member.getId()),
                () -> assertThat(memberElementList)
                        .extracting(MemberElement::getIsFollow)
                        .containsExactly(false)
        );
    }

    @DisplayName("[팔로잉 조회] 현재 유저가 차단하였을 경우 해당 유저는 제외되어 조회된다.")
    @Test
    void getFollowings_whenBlock() throws Exception {
        // given
        follow(sender, receiver);
        follow(sender, member);
        block(loginMember, receiver);

        // when
        MemberElements memberElements = followService.getFollowings(
                Accessor.member(loginMember.getId()),
                sender.getHandle(),
                PageRequest.of(0, DEFAULT_PAGING_SIZE)
        );

        // then
        List<MemberElement> memberElementList = memberElements.getMemberList();
        assertAll(
                () -> assertThat(memberElementList)
                        .extracting(MemberElement::getMemberId)
                        .containsExactly(member.getId()),
                () -> assertThat(memberElementList)
                        .extracting(MemberElement::getIsFollow)
                        .containsExactly(false)
        );
    }

    @DisplayName("[팔로워 조회] 유저를 팔로우하고 있는 유저들이 조회된다.")
    @Test
    void getFollowers() throws Exception {
        // given
        follow(sender, receiver);
        follow(member, receiver);
        follow(loginMember, sender);

        // when
        MemberElements memberElements = followService.getFollowers(
                Accessor.member(loginMember.getId()),
                receiver.getHandle(),
                PageRequest.of(0, DEFAULT_PAGING_SIZE)
        );

        // then
        List<MemberElement> memberElementList = memberElements.getMemberList();
        assertAll(
                () -> assertThat(memberElementList)
                        .extracting(MemberElement::getMemberId)
                        .containsExactly(member.getId(), sender.getId()),
                () -> assertThat(memberElementList)
                        .extracting(MemberElement::getIsFollow)
                        .containsExactly(false, true)
        );
    }

    @DisplayName("[팔로워 조회] 현재 유저를 차단하였을 경우 해당 유저는 제외되어 조회된다.")
    @Test
    void getFollowers_whenBlocked() throws Exception {
        // given
        follow(sender, receiver);
        follow(member, receiver);
        block(sender, loginMember);

        // when
        MemberElements memberElements = followService.getFollowers(
                Accessor.member(loginMember.getId()),
                receiver.getHandle(),
                PageRequest.of(0, DEFAULT_PAGING_SIZE)
        );

        // then
        List<MemberElement> memberElementList = memberElements.getMemberList();
        assertAll(
                () -> assertThat(memberElementList)
                        .extracting(MemberElement::getMemberId)
                        .containsExactly(member.getId()),
                () -> assertThat(memberElementList)
                        .extracting(MemberElement::getIsFollow)
                        .containsExactly(false)
        );
    }

    @DisplayName("[팔로워 조회] 현재 유저가 차단하였을 경우 해당 유저는 제외되어 조회된다.")
    @Test
    void getFollowers_whenBlock() throws Exception {
        // given
        follow(sender, receiver);
        follow(member, receiver);
        block(loginMember, sender);

        // when
        MemberElements memberElements = followService.getFollowers(
                Accessor.member(loginMember.getId()),
                receiver.getHandle(),
                PageRequest.of(0, DEFAULT_PAGING_SIZE)
        );

        // then
        List<MemberElement> memberElementList = memberElements.getMemberList();
        assertAll(
                () -> assertThat(memberElementList)
                        .extracting(MemberElement::getMemberId)
                        .containsExactly(member.getId()),
                () -> assertThat(memberElementList)
                        .extracting(MemberElement::getIsFollow)
                        .containsExactly(false)
        );
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
