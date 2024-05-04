package com.apps.pochak.comment.service;

import com.apps.pochak.alarm.domain.Alarm;
import com.apps.pochak.alarm.domain.repository.AlarmRepository;
import com.apps.pochak.comment.domain.Comment;
import com.apps.pochak.comment.domain.repository.CommentRepository;
import com.apps.pochak.comment.dto.request.CommentUploadRequest;
import com.apps.pochak.comment.dto.response.CommentElements;
import com.apps.pochak.comment.dto.response.ParentCommentElement;
import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.login.jwt.JwtService;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.post.domain.repository.PostRepository;
import com.apps.pochak.tag.domain.Tag;
import com.apps.pochak.tag.domain.repository.TagRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.INVALID_COMMENT_ID;
import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.INVALID_POST_ID;
import static com.apps.pochak.global.converter.PageableToPageRequestConverter.toPageRequest;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final TagRepository tagRepository;
    private final PostRepository postRepository;
    private final AlarmRepository alarmRepository;

    private final JwtService jwtService;

    @Transactional
    public CommentElements getComments(
            final Long postId,
            final Pageable pageable
    ) {
        final Member loginMember = jwtService.getLoginMember();
        final Post post = postRepository.findPublicPostById(postId);
        final Page<Comment> commentList = commentRepository.findParentCommentByPost(post, pageable);
        return new CommentElements(loginMember, commentList);
    }

    @Transactional
    public ParentCommentElement getChildCommentsByParentCommentId(
            final Long postId,
            final Long parentCommentId,
            final Pageable pageable
    ) {
        final Comment comment = commentRepository.findParentCommentById(parentCommentId)
                .orElseThrow(() -> new GeneralException(INVALID_POST_ID));
        final Post post = postRepository.findPublicPostById(postId);
        return new ParentCommentElement(comment, toPageRequest(pageable));
    }

    @Transactional
    public void saveComment(Long postId, CommentUploadRequest request) {
        final Member member = jwtService.getLoginMember();
        final Post post = postRepository.findPublicPostById(postId);

        if (isChildComment(request)) {
            saveChildComment(
                    request,
                    member,
                    post
            );
        } else {
            saveParentComment(
                    request,
                    member,
                    post
            );
        }
    }

    private Boolean isChildComment(CommentUploadRequest request) {
        return request.getParentCommentId() != null;
    }

    private Comment saveChildComment(
            final CommentUploadRequest request,
            final Member member,
            final Post post
    ) {
        final Comment parentComment = commentRepository
                .findParentCommentById(request.getParentCommentId())
                .orElseThrow(() -> new GeneralException(INVALID_COMMENT_ID));
        final Comment comment = commentRepository.save(
                request.toEntity(
                        member,
                        post,
                        parentComment
                )
        );
        final Member parentCommentWriter = parentComment.getMember();
        sendCommentReplyAlarm(comment, parentCommentWriter);

        final Member owner = post.getOwner();
        if (!owner.getId().equals(parentCommentWriter.getId())) {
            sendPostOwnerCommentAlarm(comment, owner);
        }
        sendTaggedPostCommentAlarm(comment, parentCommentWriter.getId());

        return comment;
    }

    private void saveParentComment(
            final CommentUploadRequest request,
            final Member member,
            final Post post
    ) {
        final Comment comment = commentRepository.save(request.toEntity(member, post));
        sendPostOwnerCommentAlarm(comment, post.getOwner());
        sendTaggedPostCommentAlarm(comment, 0L);
    }

    private void sendPostOwnerCommentAlarm(
            final Comment comment,
            final Member receiver
    ) {
        final Alarm alarm = Alarm.getPostOwnerCommentAlarm(
                comment,
                receiver
        );
        alarmRepository.save(alarm);
    }

    private void sendTaggedPostCommentAlarm(
            final Comment comment,
            final Long excludeMemberId
    ) {
        final List<Tag> tagList = tagRepository.findTagsByPost(comment.getPost());

        final List<Alarm> alarmList = new ArrayList<>();
        for (Tag tag : tagList) {
            final Member taggedMember = tag.getMember();
            if (!excludeMemberId.equals(taggedMember.getId())) {
                alarmList.add(
                        Alarm.getTaggedPostCommentAlarm(comment, taggedMember)
                );
            }
        }

        alarmRepository.saveAll(alarmList);
    }

    private void sendCommentReplyAlarm(
            final Comment comment,
            final Member receiver
    ) {
        final Alarm commentReplyAlarm = Alarm.getCommentReplyAlarm(
                comment,
                receiver
        );

        alarmRepository.save(commentReplyAlarm);
    }
}
