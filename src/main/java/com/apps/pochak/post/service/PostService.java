package com.apps.pochak.post.service;

import com.apps.pochak.alarm.domain.Alarm;
import com.apps.pochak.alarm.domain.repository.AlarmRepository;
import com.apps.pochak.comment.domain.Comment;
import com.apps.pochak.comment.domain.repository.CommentRepository;
import com.apps.pochak.follow.domain.repository.FollowRepository;
import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.global.s3.S3Service;
import com.apps.pochak.like.domain.repository.LikeRepository;
import com.apps.pochak.login.jwt.JwtService;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.post.domain.repository.PostRepository;
import com.apps.pochak.post.dto.PostElements;
import com.apps.pochak.post.dto.request.PostUploadRequest;
import com.apps.pochak.post.dto.response.PostDetailResponse;
import com.apps.pochak.post.dto.response.PostSearchResponse;
import com.apps.pochak.tag.domain.Tag;
import com.apps.pochak.tag.domain.repository.TagRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.*;
import static com.apps.pochak.global.s3.DirName.POST;

@Service
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
    private final JwtService jwtService;
    private final RestTemplate restTemplate;

    @Value("${lambda.search}")
    private String lambdaBaseUrl;

    public PostElements getHomeTab(Pageable pageable) {
        final Member loginMember = jwtService.getLoginMember();
        final Page<Post> taggedPost = postRepository.findTaggedPostsOfFollowing(loginMember, pageable);
        return PostElements.from(taggedPost);
    }

    public PostDetailResponse getPostDetail(final Long postId) {
        final Member loginMember = jwtService.getLoginMember();
        final Post post = postRepository.findPostById(postId, loginMember);
        final List<Tag> tagList = tagRepository.findTagsByPost(post);
        if (post.isPrivate() && !isAccessAuthorized(post, tagList, loginMember)) {
            throw new GeneralException(PRIVATE_POST);
        }
        final Boolean isFollow = post.isOwner(loginMember) ?
                null : followRepository.existsBySenderAndReceiver(loginMember, post.getOwner());
        final Boolean isLike = likeRepository.existsByLikeMemberAndLikedPost(loginMember, post);
        final int likeCount = likeRepository.countByLikedPost(post);
        final Comment comment = commentRepository.findFirstByPost(post).orElse(null);

        return PostDetailResponse.of()
                .post(post)
                .tagList(tagList)
                .isFollow(isFollow)
                .isLike(isLike)
                .likeCount(likeCount)
                .recentComment(comment)
                .build();
    }

    private Boolean isAccessAuthorized(final Post post,
                                       final List<Tag> tagList,
                                       final Member loginMember) {
        final List<String> taggedMemberHandleList = tagList.stream()
                .map(
                        tag -> tag.getMember().getHandle()
                ).collect(Collectors.toList());
        return post.isOwner(loginMember) || taggedMemberHandleList.contains(loginMember.getHandle());
    }

    @Transactional
    public void savePost(final PostUploadRequest request) {
        final Member loginMember = jwtService.getLoginMember();
        final String image = s3Service.upload(request.getPostImage(), POST);
        final Post post = request.toEntity(image, loginMember);
        postRepository.save(post);

        final List<String> taggedMemberHandles = request.getTaggedMemberHandleList();
        final List<Member> taggedMemberList = memberRepository.findMemberByHandleList(taggedMemberHandles);

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
                tag -> Alarm.getTagApprovalAlarm(
                        tag,
                        tag.getMember()
                )
        ).collect(Collectors.toList());
        alarmRepository.saveAll(tagApprovalAlarmList);
    }

    @Transactional
    public void deletePost(final Long postId) {
        final Member loginMember = jwtService.getLoginMember();
        final Post post = postRepository.findById(postId).orElseThrow(() -> new GeneralException(INVALID_POST_ID));
        if (!post.getOwner().getId().equals(loginMember.getId())) {
            throw new GeneralException(NOT_YOUR_POST);
        }
        postRepository.delete(post);
        commentRepository.bulkDeleteByPost(post);
    }

    public PostElements getSearchTab(Pageable pageable) {
        final Member loginMember = jwtService.getLoginMember();

        final PostSearchResponse response = restTemplate
                .getForObject(
                        lambdaBaseUrl + "/post_recommend_tab?userId=" + loginMember.getId(),
                        PostSearchResponse.class
                );

        final Page<Post> postPage = postRepository.findPostsInIdList(response.getPostIdList(), pageable);
        return PostElements.from(postPage);
    }
}
