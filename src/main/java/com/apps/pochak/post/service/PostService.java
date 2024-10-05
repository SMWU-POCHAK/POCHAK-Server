package com.apps.pochak.post.service;

import com.apps.pochak.alarm.domain.Alarm;
import com.apps.pochak.alarm.domain.TagAlarm;
import com.apps.pochak.alarm.domain.repository.AlarmRepository;
import com.apps.pochak.alarm.service.TagAlarmService;
import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.comment.domain.Comment;
import com.apps.pochak.comment.domain.repository.CommentRepository;
import com.apps.pochak.follow.domain.repository.FollowRepository;
import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.global.s3.S3Service;
import com.apps.pochak.like.domain.repository.LikeRepository;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.post.domain.repository.PostCustomRepository;
import com.apps.pochak.post.domain.repository.PostRepository;
import com.apps.pochak.post.dto.PostElements;
import com.apps.pochak.post.dto.request.PostUploadRequest;
import com.apps.pochak.post.dto.response.PostDetailResponse;
import com.apps.pochak.post.dto.response.PostPreviewResponse;
import com.apps.pochak.tag.domain.Tag;
import com.apps.pochak.tag.domain.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.*;
import static com.apps.pochak.global.s3.DirName.POST;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final PostCustomRepository postCustomRepository;
    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final TagRepository tagRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final AlarmRepository alarmRepository;

    private final TagAlarmService tagAlarmService;
    private final S3Service s3Service;

    private static final int MAX_TAG_COUNT = 5;

    @Transactional(readOnly = true)
    public PostElements getHomeTab(
            final Accessor accessor,
            final Pageable pageable
    ) {
        final Member loginMember = memberRepository.findMemberById(accessor.getMemberId());
        final Page<Post> taggedPost = postRepository.findTaggedPostsOfFollowing(loginMember, pageable);
        return PostElements.from(taggedPost);
    }

    @Transactional(readOnly = true)
    public PostDetailResponse getPostDetail(
            final Accessor accessor,
            final Long postId
    ) {
        final Member loginMember = memberRepository.findMemberById(accessor.getMemberId());
        final Post post = postCustomRepository.findPostByIdWithoutBlockPost(postId, accessor.getMemberId());
        final List<Tag> tagList = tagRepository.findTagsByPost(post);
        if (post.isPrivate()) {
            throw new GeneralException(PRIVATE_POST);
        }
        final Boolean isFollow = post.isOwner(loginMember) ?
                null : followRepository.existsBySenderAndReceiver(loginMember, post.getOwner());
        final Boolean isLike = likeRepository.existsByLikeMemberAndLikedPost(loginMember, post);
        final int likeCount = likeRepository.countByLikedPost(post);
        final Comment comment = commentRepository.findFirstByPost(post, loginMember).orElse(null);

        return PostDetailResponse.of()
                .post(post)
                .tagList(tagList)
                .isFollow(isFollow)
                .isLike(isLike)
                .likeCount(likeCount)
                .recentComment(comment)
                .build();
    }

    public void savePost(
            final Accessor accessor,
            final PostUploadRequest request
    ) {
        int requestTagCount = request.getTaggedMemberHandleList().size();
        if (requestTagCount > MAX_TAG_COUNT) {
            throw new GeneralException(EXCEED_TAG_LIMIT);
        }

        final Member loginMember = memberRepository.findMemberById(accessor.getMemberId());
        if (request.getTaggedMemberHandleList().contains(loginMember.getHandle())) {
            throw new GeneralException(TAGGED_ONESELF);
        }

        final String image = s3Service.upload(request.getPostImage(), POST);
        final Post post = request.toEntity(image, loginMember);
        postRepository.save(post);

        final List<String> taggedMemberHandles = request.getTaggedMemberHandleList();
        final List<Member> taggedMemberList = memberRepository.findMemberByHandleList(taggedMemberHandles, loginMember);

        int foundTagSize = taggedMemberList.size();
        if (requestTagCount != foundTagSize) {
            s3Service.deleteFileFromS3(image);
            throw new GeneralException(INVALID_TAG_INFO);
        }

        final List<Tag> tagList = saveTags(taggedMemberList, post);
        tagAlarmService.saveTagApprovalAlarms(tagList, loginMember);
    }

    private List<Tag> saveTags(
            final List<Member> taggedMemberList,
            final Post post
    ) {
        final List<Tag> tagList = taggedMemberList.stream().map(
                member -> Tag.builder()
                        .member(member)
                        .post(post)
                        .build()
        ).collect(Collectors.toList());
        return tagRepository.saveAll(tagList);
    }

    public void deletePost(
            final Accessor accessor,
            final Long postId
    ) {
        final Member loginMember = memberRepository.findMemberById(accessor.getMemberId());
        final Post post = postRepository.findById(postId).orElseThrow(() -> new GeneralException(INVALID_POST_ID));
        checkAuthorized(post, loginMember);
        postRepository.delete(post);
        commentRepository.deleteByPost(post);
        tagRepository.deleteByPost(post);
        likeRepository.deleteByLikedPost(post);
        alarmRepository.deleteByPost(post.getId());
    }


    private void checkAuthorized(
            final Post post,
            final Member member
    ) {
        if (post.isOwner(member)) return;

        List<Tag> tagList = tagRepository.findTagsByPost(post);
        for (Tag tag : tagList) {
            if (tag.isMember(member)) return;
        }

        throw new GeneralException(NOT_YOUR_POST);
    }


    @Transactional(readOnly = true)
    public PostElements getSearchTab(
            final Accessor accessor,
            final Pageable pageable
    ) {
        final Page<Post> postPage = postRepository.findPopularPost(pageable);
        return PostElements.from(postPage);
    }

    @Transactional(readOnly = true)
    public PostPreviewResponse getPreviewPost(
            final Accessor accessor,
            final Long alarmId
    ) {
        Member loginMember = memberRepository.findMemberById(accessor.getMemberId());
        Alarm alarm = alarmRepository.findAlarmByIdAndReceiver(alarmId, loginMember)
                .orElseThrow(() -> new GeneralException(INVALID_ALARM_ID));

        if (!(alarm instanceof TagAlarm tagAlarm)) throw new GeneralException(CANNOT_PREVIEW);

        Post previewPost = postRepository.findPostByTag(tagAlarm.getTag()).orElseThrow(() -> new GeneralException(INVALID_POST_ID));
        List<Tag> tagList = tagRepository.findTagsByPost(previewPost);

        return new PostPreviewResponse(previewPost, tagList);
    }
}
