package com.apps.pochak.memories.service;

import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.follow.domain.Follow;
import com.apps.pochak.follow.domain.repository.FollowRepository;
import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.memories.dto.response.MemoriesPreviewResponse;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.post.domain.repository.PostRepository;
import com.apps.pochak.tag.domain.Tag;
import com.apps.pochak.tag.domain.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.NULL_POST;

@Service
@Transactional
@RequiredArgsConstructor
public class MemoriesService {
    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final TagRepository tagRepository;
    private final PostRepository postRepository;

    public MemoriesPreviewResponse getMemories(Accessor accessor, String handle) {
        Member loginMember = memberRepository.findMemberById(accessor.getMemberId());
        Member member = memberRepository.findByHandle(handle, loginMember);
        Follow follow = followRepository.findBySenderAndReceiver(loginMember, member);
        Follow followed = followRepository.findBySenderAndReceiver(member, loginMember);
        Page<Tag> tagged = tagRepository.findFirstTag(member, loginMember, PageRequest.of(0, 1));
        Page<Tag> tag = tagRepository.findFirstTag(loginMember, member, PageRequest.of(0, 1));
        Page<Tag> taggedWith = tagRepository.findFirstTaggedWith(loginMember, member, PageRequest.of(0, 1));
        Page<Tag> tagAll = tagRepository.findLatestTagged(loginMember, member, PageRequest.of(0, 1));

        Tag firstTagged = (tagged.hasContent() ? tagged.getContent().get(0) : null);
        Tag firstTag = (tag.hasContent() ? tag.getContent().get(0) : null);
        Tag firstTaggedWith = (taggedWith.hasContent() ? taggedWith.getContent().get(0) : null);
        Tag latestTag = (tagAll.hasContent() ? tagAll.getContent().get(0) : null);

        return MemoriesPreviewResponse.of()
                .loginMember(loginMember)
                .member(member)
                .follow(follow)
                .followed(followed)
                .firstPochaked(firstTagged)
                .firstPochak(firstTag)
                .firstPochackedWith(firstTaggedWith)
                .latestPost(latestTag)
                .build();
    }
}
