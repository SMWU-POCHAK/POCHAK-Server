package com.apps.pochak.follow.service;

import com.apps.pochak.alarm.domain.Alarm;
import com.apps.pochak.alarm.domain.repository.AlarmRepository;
import com.apps.pochak.follow.domain.Follow;
import com.apps.pochak.follow.domain.repository.FollowRepository;
import com.apps.pochak.global.api_payload.code.BaseCode;
import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.login.jwt.JwtService;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.CustomMemberRepository;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.member.dto.response.MemberElement;
import com.apps.pochak.member.dto.response.MemberElements;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.apps.pochak.global.BaseEntityStatus.ACTIVE;
import static com.apps.pochak.global.BaseEntityStatus.DELETED;
import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.*;
import static com.apps.pochak.global.api_payload.code.status.SuccessStatus.*;

@Service
@RequiredArgsConstructor
public class FollowService {
    private final FollowRepository followRepository;
    private final AlarmRepository alarmRepository;
    private final MemberRepository memberRepository;
    private final CustomMemberRepository customMemberRepository;
    private final JwtService jwtService;

    @Transactional
    public BaseCode follow(final String handle) {
        final Member loginMember = jwtService.getLoginMember();
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
            deleteFollowAlarm(follow);
            return SUCCESS_UNFOLLOW;
        } else {
            follow.setStatus(ACTIVE);
            sendFollowAlarm(follow, follow.getReceiver());
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
        sendFollowAlarm(follow, receiver);
        return SUCCESS_FOLLOW;
    }

    private void sendFollowAlarm(
            final Follow follow,
            final Member receiver
    ) {
        final Alarm followAlarm = Alarm.getFollowAlarm(
                follow,
                receiver
        );
        alarmRepository.save(followAlarm);
    }

    private void deleteFollowAlarm(final Follow follow) {
        final List<Alarm> alarmList = alarmRepository.findAlarmByFollow(follow);
        alarmRepository.deleteAll(alarmList);
    }

    @Transactional
    public BaseCode deleteFollower(final String handle,
                                   final String followerHandle
    ) {
        // TODO: Refactor permission checking part using annotations.
        final Member loginMember = jwtService.getLoginMember();
        if (!loginMember.getHandle().equals(handle)) {
            throw new GeneralException(_UNAUTHORIZED);
        }

        final Member follower = memberRepository.findByHandle(followerHandle, loginMember);
        final Follow follow = followRepository.findBySenderAndReceiver(follower, loginMember);
        if (!follow.isFollow()) {
            throw new GeneralException(NOT_FOLLOW);
        }
        follow.toggleCurrentStatus();
        return SUCCESS_DELETE_FOLLOWER;
    }

    public MemberElements getFollowings(final String handle,
                                        final Pageable pageable
    ) {
        final Member loginMember = jwtService.getLoginMember();
        final Member member = memberRepository.findByHandle(handle, loginMember);
        final Page<MemberElement> followingPage = customMemberRepository.findFollowingsAndIsFollow(
                member,
                loginMember.getId(),
                pageable
        );

        return MemberElements.from()
                .memberElementPage(followingPage)
                .build();
    }

    public MemberElements getFollowers(final String handle,
                                       final Pageable pageable
    ) {
        final Member loginMember = jwtService.getLoginMember();
        final Member member = memberRepository.findByHandle(handle, loginMember);
        final Page<MemberElement> followerPage = customMemberRepository.findFollowersAndIsFollow(
                member,
                loginMember.getId(),
                pageable
        );

        return MemberElements.from()
                .memberElementPage(followerPage)
                .build();
    }
}
