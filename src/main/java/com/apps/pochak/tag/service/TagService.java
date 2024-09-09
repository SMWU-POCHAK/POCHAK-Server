package com.apps.pochak.tag.service;

import com.apps.pochak.alarm.service.TagAlarmService;
import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.global.api_payload.code.BaseCode;
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

import static com.apps.pochak.global.api_payload.code.status.SuccessStatus.*;

@Service
@Transactional
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    private final TagAlarmService tagAlarmService;

    public BaseCode approveOrRejectTagRequest(
            final Accessor accessor,
            final Long tagId,
            final Boolean isAccept
    ) {
        final Member loginMember = memberRepository.findMemberById(accessor.getMemberId());
        final Tag tag = tagRepository.findTagByIdAndMember(tagId, loginMember);
        if (isAccept) {
            return acceptPost(tag);
        } else
            return rejectPost(tag);
    }

    private BaseCode acceptPost(final Tag tag) {
        tag.setIsAccepted(true);
        tagAlarmService.deleteAlarmByTag(tag);

        final Post post = tag.getPost();
        final List<Tag> tagList = tagRepository.findTagsByPost(post);

        final boolean currentTagApprovalStatus = tagList.stream().allMatch(Tag::getIsAccepted);
        if (currentTagApprovalStatus) {
            post.makePublic();
            return SUCCESS_POST_ACCEPT;
        }
        return SUCCESS_ACCEPT;
    }

    private BaseCode rejectPost(final Tag tag) {
        final Post post = tag.getPost();
        final List<Tag> tagList = tagRepository.findTagsByPost(post);

        tagAlarmService.deleteAlarmByTagList(tagList);
        tagRepository.deleteAll(tagList);
        postRepository.delete(post);

        return SUCCESS_REJECT;
    }
}
