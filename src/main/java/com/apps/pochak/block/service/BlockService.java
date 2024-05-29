package com.apps.pochak.block.service;

import com.apps.pochak.block.domain.Block;
import com.apps.pochak.block.domain.repository.BlockRepository;
import com.apps.pochak.follow.domain.repository.FollowRepository;
import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.like.domain.repository.LikeRepository;
import com.apps.pochak.login.jwt.JwtService;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.post.domain.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.BLOCK_ONESELF;

@Service
@RequiredArgsConstructor
public class BlockService {
    private final BlockRepository blockRepository;
    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;

    private final JwtService jwtService;

    @Transactional
    public void blockMember(String handle) {
        Member blocker = jwtService.getLoginMember();
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
}
