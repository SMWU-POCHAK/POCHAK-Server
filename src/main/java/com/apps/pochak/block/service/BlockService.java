package com.apps.pochak.block.service;

import com.apps.pochak.auth.Auth;
import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.block.domain.Block;
import com.apps.pochak.block.domain.repository.BlockRepository;
import com.apps.pochak.block.dto.response.BlockElements;
import com.apps.pochak.follow.domain.repository.FollowRepository;
import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.like.domain.repository.LikeRepository;
import com.apps.pochak.login.provider.JwtProvider;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.post.domain.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.BLOCK_ONESELF;
import static com.apps.pochak.global.api_payload.code.status.ErrorStatus._UNAUTHORIZED;

@Service
@RequiredArgsConstructor
@Transactional
public class BlockService {
    private final BlockRepository blockRepository;
    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;

    public void blockMember(
            final Accessor accessor,
            final String handle
    ) {
        Member blocker = memberRepository.findMemberById(accessor.getMemberId());
        Member blockedMember = memberRepository.findByHandle(handle, blocker);

        if (blocker.getId().equals(blockedMember.getId())) {
            throw new GeneralException(BLOCK_ONESELF);
        }

        saveBlockEntity(blockedMember, blocker);
        followRepository.deleteFollowsBetweenMembers(blockedMember, blocker);
        likeRepository.deleteLikesBetweenMembers(blockedMember, blocker);
        postRepository.setPostInactiveBetweenMembers(blockedMember, blocker);
    }

    private void saveBlockEntity(
            final Member blockedMember,
            final Member blocker
    ) {
        Block block = Block.builder()
                .blockedMember(blockedMember)
                .blocker(blocker)
                .build();

        blockRepository.save(block);
    }

    @Transactional(readOnly = true)
    public BlockElements getBlockedMember(
            final Accessor accessor,
            final String handle,
            final Pageable pageable
    ) {
        Member loginMember = memberRepository.findMemberById(accessor.getMemberId());
        Member member = memberRepository.findByHandleWithoutLogin(handle);

        if (!member.equals(loginMember)) {
            throw new GeneralException(_UNAUTHORIZED);
        }

        Page<Block> blockedMemberPage = blockRepository.findBlockByBlocker(member, pageable);

        return BlockElements.from()
                .blockPage(blockedMemberPage)
                .build();
    }

    public void cancelBlock(
            final Accessor accessor,
            final String handle,
            final String blockedMemberHandle
    ) {
        Member loginMember = memberRepository.findMemberById(accessor.getMemberId());
        Member blocker = memberRepository.findByHandleWithoutLogin(handle);

        if (!loginMember.equals(blocker)) {
            throw new GeneralException(_UNAUTHORIZED);
        }

        Member blockedMember = memberRepository.findByHandleWithoutLogin(blockedMemberHandle);

        blockRepository.deleteByBlockerAndBlockedMember(blocker, blockedMember);
        postRepository.reactivatePostBetweenMembers(blocker, blockedMember);
    }
}
