package com.apps.pochak.tag.service;

import com.apps.pochak.alarm.domain.repository.AlarmRepository;
import com.apps.pochak.global.api_payload.code.BaseCode;
import com.apps.pochak.login.provider.JwtProvider;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.post.domain.repository.PostRepository;
import com.apps.pochak.tag.domain.Tag;
import com.apps.pochak.tag.domain.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.apps.pochak.global.api_payload.code.status.SuccessStatus.*;

@Service
@Transactional
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;
    private final AlarmRepository alarmRepository;
    private final PostRepository postRepository;

    private final JwtProvider jwtProvider;

    public BaseCode approveOrRejectTagRequest(final Long tagId, final Boolean isAccept) {
        final Member loginMember = jwtProvider.getLoginMember();
        final Tag tag = tagRepository.findTagByIdAndMember(tagId, loginMember);
        if (isAccept) {
            return acceptPost(tag);
        } else
            return rejectPost(tag);
    }

    private BaseCode acceptPost(final Tag tag) {
        tag.setIsAccepted(true);
        alarmRepository.deleteAlarmByTag(tag.getId());

        final Post post = tag.getPost();
        final List<Tag> tagList = tagRepository.findTagsByPost(post);

        final boolean currentTagApprovalStatus = tagList.stream().allMatch(Tag::getIsAccepted);
        if (currentTagApprovalStatus) {
            post.makePublic();
            return SUCCESS_POST_ACCEPT;
        }
        return SUCCESS_ACCEPT;
    }

    private BaseCode rejectPost(Tag tag) {
        final Post post = tag.getPost();
        final List<Tag> tagList = tagRepository.findTagsByPost(post);

        List<Long> tagIdList = tagList.stream().map(
                Tag::getId
        ).collect(Collectors.toList());

        alarmRepository.deleteAlarmByTagIdList(tagIdList);
        tagRepository.deleteAll(tagList);
        postRepository.delete(post);

        return SUCCESS_REJECT;
    }
}
