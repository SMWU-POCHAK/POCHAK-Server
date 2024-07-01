package com.apps.pochak.tag.service;

import com.apps.pochak.alarm.domain.Alarm;
import com.apps.pochak.alarm.domain.repository.AlarmRepository;
import com.apps.pochak.global.api_payload.code.BaseCode;
import com.apps.pochak.login.jwt.JwtService;
import com.apps.pochak.member.domain.Member;
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
    private final AlarmRepository alarmRepository;
    private final PostRepository postRepository;

    private final JwtService jwtService;

    public BaseCode approveOrRejectTagRequest(Long tagId, Boolean isAccept) {
        final Member loginMember = jwtService.getLoginMember();
        final Tag tag = tagRepository.findTagByIdAndMember(tagId, loginMember);
        if (isAccept) {
            return acceptPost(tag);
        } else
            return rejectPost(tag);
    }

    private BaseCode acceptPost(Tag tag) {
        tag.setIsAccepted(true);
        alarmRepository.deleteAlarmByTag(tag);

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

        alarmRepository.deleteAlarmByTagList(tagList);
        tagRepository.deleteAll(tagList);
        postRepository.delete(post);

        return SUCCESS_REJECT;
    }
}
