package com.apps.pochak.post.service;

import com.apps.pochak.alarm.domain.Alarm;
import com.apps.pochak.alarm.domain.TagAlarm;
import com.apps.pochak.alarm.domain.repository.AlarmRepository;
import com.apps.pochak.comment.domain.Comment;
import com.apps.pochak.comment.domain.repository.CommentRepository;
import com.apps.pochak.follow.domain.repository.FollowRepository;
import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.global.s3.S3Service;
import com.apps.pochak.like.domain.repository.LikeRepository;
import com.apps.pochak.login.provider.JwtProvider;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.post.domain.Post;
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
    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final TagRepository tagRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final AlarmRepository alarmRepository;

    private final S3Service s3Service;
    private final JwtProvider jwtProvider;

    @Transactional(readOnly = true)
    public PostElements getHomeTab(Pageable pageable) {
        final Member loginMember = jwtProvider.getLoginMember();
        final Page<Post> taggedPost = postRepository.findTaggedPostsOfFollowing(loginMember, pageable);
        return PostElements.from(taggedPost);
    }

    @Transactional(readOnly = true)
    public PostDetailResponse getPostDetail(final Long postId) {
        final Member loginMember = jwtProvider.getLoginMember();
        final Post post = postRepository.findPostById(postId, loginMember);
        final List<Tag> tagList = tagRepository.findTagsByPost(post);
        if (post.isPrivate() && !isAccessAuthorized(post, tagList, loginMember)) {
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

    private boolean isAccessAuthorized(final Post post,
                                       final List<Tag> tagList,
                                       final Member loginMember) {
        final List<String> taggedMemberHandleList = tagList.stream()
                .map(
                        tag -> tag.getMember().getHandle()
                ).collect(Collectors.toList());
        return post.isOwner(loginMember) || taggedMemberHandleList.contains(loginMember.getHandle());
    }

    public void savePost(final PostUploadRequest request) {
        final Member loginMember = jwtProvider.getLoginMember();
        if (request.getTaggedMemberHandleList().contains(loginMember.getHandle())) {
            throw new GeneralException(TAGGED_ONESELF);
        }

        final String image = s3Service.upload(request.getPostImage(), POST);
        final Post post = request.toEntity(image, loginMember);
        postRepository.save(post);

        final List<String> taggedMemberHandles = request.getTaggedMemberHandleList();
        final List<Member> taggedMemberList = memberRepository.findMemberByHandleList(taggedMemberHandles, loginMember);

        final List<Tag> tagList = saveTags(taggedMemberList, post);
        saveTagApprovalAlarms(tagList);
    }

    private List<Tag> saveTags(List<Member> taggedMemberList, Post post) {
        final List<Tag> tagList = taggedMemberList.stream().map(
                member -> Tag.builder()
                        .member(member)
                        .post(post)
                        .build()
        ).collect(Collectors.toList());
        return tagRepository.saveAll(tagList);
    }

    private void saveTagApprovalAlarms(List<Tag> tagList) {
        final List<Alarm> tagApprovalAlarmList = tagList.stream().map(
                tag -> new TagAlarm(tag, tag.getMember())
        ).collect(Collectors.toList());
        alarmRepository.saveAll(tagApprovalAlarmList);
    }

    public void deletePost(final Long postId) {
        final Member loginMember = jwtProvider.getLoginMember();
        final Post post = postRepository.findById(postId).orElseThrow(() -> new GeneralException(INVALID_POST_ID));
        if (!post.getOwner().getId().equals(loginMember.getId())) {
            throw new GeneralException(NOT_YOUR_POST);
        }
        postRepository.delete(post);
        commentRepository.bulkDeleteByPost(post);
    }

    @Transactional(readOnly = true)
    public PostElements getSearchTab(Pageable pageable) {
        final Page<Post> postPage = postRepository.findPopularPost(pageable);
        return PostElements.from(postPage);
    }

    @Transactional(readOnly = true)
    public PostPreviewResponse getPreviewPost(final Long alarmId) {
        Member loginMember = jwtProvider.getLoginMember();
        Alarm alarm = alarmRepository.findAlarmByIdAndReceiver(alarmId, loginMember)
                .orElseThrow(() -> new GeneralException(INVALID_ALARM_ID));

        if (!(alarm instanceof TagAlarm tagAlarm)) throw new GeneralException(CANNOT_PREVIEW);

        Post previewPost = postRepository.findPostByTag(tagAlarm.getTag()).orElseThrow(() -> new GeneralException(INVALID_POST_ID));
        List<Tag> tagList = tagRepository.findTagsByPost(previewPost);

        return new PostPreviewResponse(previewPost, tagList);
    }
}
