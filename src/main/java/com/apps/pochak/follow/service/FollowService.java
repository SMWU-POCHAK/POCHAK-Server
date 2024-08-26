package com.apps.pochak.follow.service;

import com.apps.pochak.alarm.service.FollowAlarmService;
import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.follow.domain.Follow;
import com.apps.pochak.follow.domain.repository.FollowRepository;
import com.apps.pochak.global.api_payload.code.BaseCode;
import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.CustomMemberRepository;
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
    private final CustomMemberRepository customMemberRepository;
    private final FollowAlarmService followAlarmService;

    public BaseCode follow(
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
            return toggleFollowStatus(follow);
        } else {
            return createAndSaveFollow(loginMember, member);
        }
    }

    private BaseCode toggleFollowStatus(Follow follow) {
        if (follow.getStatus().equals(ACTIVE)) {
            follow.setStatus(DELETED);
            followAlarmService.deleteFollowAlarm(follow);
            return SUCCESS_UNFOLLOW;
        } else {
            follow.setStatus(ACTIVE);
            followAlarmService.sendFollowAlarm(follow, follow.getReceiver());
            return SUCCESS_FOLLOW;
        }
    }

    private BaseCode createAndSaveFollow(
            final Member sender,
            final Member receiver
    ) {
        final Follow newFollow = Follow.of()
                .sender(sender)
                .receiver(receiver)
                .build();
        final Follow follow = followRepository.save(newFollow);
        followAlarmService.sendFollowAlarm(follow, receiver);
        return SUCCESS_FOLLOW;
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
        final Member loginMember = memberRepository.findMemberById(accessor.getMemberId());
        final Member member = memberRepository.findByHandle(handle, loginMember);
        final Page<MemberElement> followingPage = customMemberRepository.findFollowingsAndIsFollow(
                member,
                loginMember.getId(),
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
        final Member loginMember = memberRepository.findMemberById(accessor.getMemberId());
        final Member member = memberRepository.findByHandle(handle, loginMember);
        final Page<MemberElement> followerPage = customMemberRepository.findFollowersAndIsFollow(
                member,
                loginMember.getId(),
                pageable
        );

        return new MemberElements(followerPage);
    }
}
