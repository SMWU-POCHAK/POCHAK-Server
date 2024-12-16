package com.apps.pochak.follow.service;

import com.apps.pochak.alarm.service.FollowAlarmService;
import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.follow.domain.Follow;
import com.apps.pochak.follow.domain.repository.FollowRepository;
import com.apps.pochak.global.api_payload.code.BaseCode;
import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberFollowCustomRepository;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.member.dto.response.MemberElement;
import com.apps.pochak.member.dto.response.MemberElements;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.apps.pochak.global.BaseEntityStatus.ACTIVE;
import static com.apps.pochak.global.BaseEntityStatus.DELETED;
import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.*;
import static com.apps.pochak.global.api_payload.code.status.SuccessStatus.SUCCESS_FOLLOW;
import static com.apps.pochak.global.api_payload.code.status.SuccessStatus.SUCCESS_UNFOLLOW;

@Service
@Transactional
@RequiredArgsConstructor
public class FollowService {
    private final FollowRepository followRepository;
    private final MemberRepository memberRepository;
    private final MemberFollowCustomRepository memberFollowCustomRepository;
    private final FollowAlarmService followAlarmService;

    public void follow(
            final Accessor accessor,
            final String handle
    ) {
        final Member loginMember = memberRepository.findMemberById(accessor.getMemberId());
        final Member member = memberRepository.findByHandle(handle, loginMember);

        if (loginMember.getId().equals(member.getId())) {
            throw new GeneralException(FOLLOW_ONESELF);
        }

        final Optional<Follow> followOptional = followRepository.findFollowBySenderAndReceiver(loginMember, member);
        if (followOptional.isPresent()) {
            final Follow follow = followOptional.get();
            toggleFollowStatus(follow);
        } else {
            createAndSaveFollow(loginMember, member);
        }
    }

    private void toggleFollowStatus(Follow follow) {
        follow.toggleCurrentStatus();
        if (follow.getStatus().equals(ACTIVE)) {
            followAlarmService.deleteFollowAlarm(follow);
        } else {
            followAlarmService.sendFollowAlarm(follow, follow.getReceiver());
        }
    }

    private void createAndSaveFollow(
            final Member sender,
            final Member receiver
    ) {
        final Follow newFollow = Follow.of()
                .sender(sender)
                .receiver(receiver)
                .build();
        final Follow follow = followRepository.save(newFollow);
        followAlarmService.sendFollowAlarm(follow, receiver);
    }

    public void deleteFollower(
            final Accessor accessor,
            final String handle,
            final String followerHandle
    ) {
        final Member loginMember = memberRepository.findMemberById(accessor.getMemberId());
        if (!loginMember.getHandle().equals(handle)) {
            throw new GeneralException(_UNAUTHORIZED);
        }

        final Member follower = memberRepository.findByHandle(followerHandle, loginMember);
        final Follow follow = followRepository.findBySenderAndReceiver(follower, loginMember);
        if (!follow.isFollow()) {
            throw new GeneralException(NOT_FOLLOW);
        }
        follow.toggleCurrentStatus();
    }

    @Transactional(readOnly = true)
    public MemberElements getFollowings(
            final Accessor accessor,
            final String handle,
            final Pageable pageable
    ) {
        final Member member = memberRepository.findByHandleWithoutLogin(handle);
        final Page<MemberElement> followingPage = memberFollowCustomRepository.findFollowingsOfMemberAndIsFollow(
                member.getId(),
                accessor.getMemberId(),
                pageable
        );

        return new MemberElements(followingPage);
    }

    @Transactional(readOnly = true)
    public MemberElements getFollowers(
            final Accessor accessor,
            final String handle,
            final Pageable pageable
    ) {
        final Member member = memberRepository.findByHandleWithoutLogin(handle);
        final Page<MemberElement> followerPage = memberFollowCustomRepository.findFollowersOfMemberAndIsFollow(
                member.getId(),
                accessor.getMemberId(),
                pageable
        );

        return new MemberElements(followerPage);
    }
}
