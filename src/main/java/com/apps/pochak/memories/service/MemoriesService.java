package com.apps.pochak.memories.service;

import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.follow.domain.Follow;
import com.apps.pochak.follow.domain.repository.FollowRepository;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.memories.dto.response.MemoriesPostResponse;
import com.apps.pochak.memories.dto.response.MemoriesPreviewResponse;
import com.apps.pochak.post.domain.repository.PostRepository;
import com.apps.pochak.tag.domain.Tag;
import com.apps.pochak.tag.domain.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.apps.pochak.global.util.PageUtil.getFirstContentFromPage;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemoriesService {
    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final TagRepository tagRepository;
    private final PostRepository postRepository;

    public MemoriesPreviewResponse getMemories(final Accessor accessor, final String handle) {
        final Member loginMember = memberRepository.findMemberById(accessor.getMemberId());
        final Member member = memberRepository.findByHandle(handle, loginMember);
        final Follow follow = followRepository.findBySenderAndReceiver(loginMember, member);
        final Follow followed = followRepository.findBySenderAndReceiver(member, loginMember);
        final Page<Tag> tagged = tagRepository.findTagByOwnerAndMember(member, loginMember, PageRequest.of(0, 1));
        final Page<Tag> tag = tagRepository.findTagByOwnerAndMember(loginMember, member, PageRequest.of(0, 1));
        final Page<Tag> taggedWithAsc= tagRepository.findTaggedWith(loginMember, member, PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "post.allowedDate")));
        final Long countTagged = tagged.getTotalElements();
        final Long countTag = tag.getTotalElements();
        final Long countTaggedWith = taggedWithAsc.getTotalElements();
        final Page<Tag> taggedWithDesc = tagRepository.findTaggedWith(loginMember, member, PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "post.allowedDate")));
        final Page<Tag> tagOrTagged = tagRepository.findLatestTagged(loginMember, member, PageRequest.of(0, 1));

        final Tag firstTagged = getFirstContentFromPage(tagged);
        final Tag firstTag = getFirstContentFromPage(tag);
        final Tag firstTaggedWith = getFirstContentFromPage(taggedWithAsc);
        final Tag latestTaggedWith = getFirstContentFromPage(taggedWithDesc);
        final Tag latestTagOrTagged = getFirstContentFromPage(tagOrTagged);

        final Tag latestTag = findLatestTag(latestTaggedWith, latestTagOrTagged);

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

    private Tag findLatestTag(final Tag latestTaggedWith, final Tag latestTagOrTagged) {
        if (latestTaggedWith != null && latestTagOrTagged != null) {
            return latestTaggedWith.getPost().getAllowedDate().isAfter(latestTagOrTagged.getPost().getAllowedDate()) ?
                    latestTaggedWith : latestTagOrTagged;
        } else if (latestTaggedWith == null && latestTagOrTagged != null) {
            return latestTagOrTagged;
        } else return latestTaggedWith;
    }

    public MemoriesPostResponse getPochak(Accessor accessor, String handle, Pageable pageable) {
        final Member loginMember = memberRepository.findMemberById(accessor.getMemberId());
        final Member member = memberRepository.findByHandle(handle, loginMember);

        final Page<Tag> tag = tagRepository.findTagByOwnerAndMember(loginMember, member, pageable);
        return MemoriesPostResponse.from(tag);
    }

    public MemoriesPostResponse getPochaked(Accessor accessor, String handle, Pageable pageable) {
        final Member loginMember = memberRepository.findMemberById(accessor.getMemberId());
        final Member member = memberRepository.findByHandle(handle, loginMember);

        final Page<Tag> tag = tagRepository.findTagByOwnerAndMember(member, loginMember, pageable);
        return MemoriesPostResponse.from(tag);
    }

    public MemoriesPostResponse getBonded(Accessor accessor, String handle, Pageable pageable) {
        final Member loginMember = memberRepository.findMemberById(accessor.getMemberId());
        final Member member = memberRepository.findByHandle(handle, loginMember);

        final Page<Tag> tag = tagRepository.findTaggedWith(loginMember, member, pageable);
        return MemoriesPostResponse.from(tag);
    }
}
