package com.apps.pochak.tag.service;

import com.apps.pochak.alarm.service.TagAlarmService;
import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.post.domain.repository.PostRepository;
import com.apps.pochak.tag.domain.Tag;
import com.apps.pochak.tag.domain.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    private final TagAlarmService tagAlarmService;

    public void approveOrRejectTagRequest(
            final Accessor accessor,
            final Long tagId,
            final Boolean isAccept
    ) {
        final Member loginMember = memberRepository.findMemberById(accessor.getMemberId());
        final Tag tag = tagRepository.findTagByIdAndMember(tagId, loginMember);
        if (isAccept) acceptPost(tag);
        else rejectPost(tag);
    }

    private void acceptPost(final Tag tag) {
        tag.setIsAccepted(true);
        tagAlarmService.deleteAlarmByTag(tag);

        final Post post = tag.getPost();
        final List<Tag> tagList = tagRepository.findTagsByPost(post);

        final boolean currentTagApprovalStatus = tagList.stream().allMatch(Tag::getIsAccepted);
        if (currentTagApprovalStatus) {
            post.makePublic();
        }
    }

    private void rejectPost(final Tag tag) {
        final Post post = tag.getPost();
        final List<Tag> tagList = tagRepository.findTagsByPost(post);

        tagAlarmService.deleteAlarmByTagList(tagList);
        tagRepository.deleteAll(tagList);
        postRepository.delete(post);
    }
}
