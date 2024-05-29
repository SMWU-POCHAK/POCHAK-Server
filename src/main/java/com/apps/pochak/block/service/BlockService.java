package com.apps.pochak.block.service;

import com.apps.pochak.block.domain.Block;
import com.apps.pochak.block.domain.repository.BlockRepository;
import com.apps.pochak.follow.domain.repository.FollowRepository;
import com.apps.pochak.like.domain.repository.LikeRepository;
import com.apps.pochak.login.jwt.JwtService;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BlockService {
    private final BlockRepository blockRepository;
    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final LikeRepository likeRepository;

    private final JwtService jwtService;

    @Transactional
    public void blockMember(String handle) {
        Member blockedMember = memberRepository.findByHandle(handle);
        Member blocker = jwtService.getLoginMember();
        saveBlockEntity(blockedMember, blocker);
        followRepository.deleteFollowsBetweenMembers(blockedMember, blocker);
        likeRepository.deleteLikesBetweenMembers(blockedMember, blocker);
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
}
