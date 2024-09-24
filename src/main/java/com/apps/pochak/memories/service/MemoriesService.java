package com.apps.pochak.memories.service;

import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.follow.domain.Follow;
import com.apps.pochak.follow.domain.repository.FollowRepository;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.memories.dto.response.MemoriesPreviewResponse;
import com.apps.pochak.post.domain.PostStatus;
import com.apps.pochak.post.domain.repository.PostRepository;
import com.apps.pochak.tag.domain.Tag;
import com.apps.pochak.tag.domain.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Long countTag = tagRepository.countByPost_PostStatusAndPost_OwnerAndMember(PostStatus.PUBLIC, loginMember, member);
        Long countTaggedWith = tagRepository.countByMember(loginMember, member);
        Long countTagged = tagRepository.countByPost_PostStatusAndPost_OwnerAndMember(PostStatus.PUBLIC, member, loginMember);
        Page<Tag> tagged = tagRepository.findTagByMember(member, loginMember, PageRequest.of(0, 1));
        Page<Tag> tag = tagRepository.findTagByMember(loginMember, member, PageRequest.of(0, 1));
        Page<Tag> taggedWithAsc = tagRepository.findTaggedWith(loginMember, member, PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "post.allowedDate")));
        Page<Tag> taggedWithDesc = tagRepository.findTaggedWith(loginMember, member, PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "post.allowedDate")));
        Page<Tag> tagOrTagged = tagRepository.findLatestTagged(loginMember, member, PageRequest.of(0, 1));

        Tag firstTagged = (tagged.hasContent() ? tagged.getContent().get(0) : null);
        Tag firstTag = (tag.hasContent() ? tag.getContent().get(0) : null);
        Tag firstTaggedWith = (taggedWithAsc.hasContent() ? taggedWithAsc.getContent().get(0) : null);
        Tag latestTaggedWith = (taggedWithDesc.hasContent() ? taggedWithDesc.getContent().get(0) : null);
        Tag latestTagOrTagged = (tagOrTagged.hasContent() ? tagOrTagged.getContent().get(0) : null);

        Tag latestTag = null;
        if (latestTaggedWith != null && latestTagOrTagged != null) {
            latestTag = latestTaggedWith.getPost().getAllowedDate().isAfter(latestTagOrTagged.getPost().getAllowedDate()) ? latestTaggedWith : latestTagOrTagged;
        } else if (latestTaggedWith == null && latestTagOrTagged != null) {
            latestTag = latestTagOrTagged;
        } else if (latestTaggedWith != null) {
            latestTag = latestTaggedWith;
        }

        return MemoriesPreviewResponse.of()
                .loginMember(loginMember)
                .member(member)
                .follow(follow)
                .followed(followed)
                .countTag(countTag)
                .countTaggedWith(countTaggedWith)
                .countTagged(countTagged)
                .firstTagged(firstTagged)
                .firstTag(firstTag)
                .firstTaggedWith(firstTaggedWith)
                .latestTag(latestTag)
                .build();
    }
}
