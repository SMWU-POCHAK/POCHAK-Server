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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.*;
import static com.apps.pochak.global.converter.PageableToPageRequestConverter.toPageRequest;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {
    private final CommentRepository commentRepository;
    private final TagRepository tagRepository;
    private final PostRepository postRepository;
    private final AlarmRepository alarmRepository;

    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public CommentElements getComments(
            final Long postId,
            final Pageable pageable
    ) {
        final Member loginMember = jwtService.getLoginMember();
        final Post post = postRepository.findPublicPostById(postId, loginMember);
        final Page<Comment> commentList = commentRepository.findParentCommentByPost(post, loginMember, pageable);
        return new CommentElements(loginMember, commentList);
    }

    @Transactional(readOnly = true)
    public ParentCommentElement getChildCommentsByParentCommentId(
            final Long postId,
            final Long parentCommentId,
            final Pageable pageable
    ) {
        final Member loginMember = jwtService.getLoginMember();
        final Comment comment = commentRepository.findParentCommentById(parentCommentId, loginMember)
                .orElseThrow(() -> new GeneralException(INVALID_POST_ID));
        return new ParentCommentElement(comment, toPageRequest(pageable));
    }

    public void saveComment(
            final Long postId,
            final CommentUploadRequest request
    ) {
        final Member loginMember = jwtService.getLoginMember();
        final Post post = postRepository.findPublicPostById(postId, loginMember);

        if (request.checkChildComment()) {
            saveChildComment(
                    request,
                    loginMember,
                    post
            );
        } else {
            saveParentComment(
                    request,
                    loginMember,
                    post
            );
        }
    }

    private void saveChildComment(
            final CommentUploadRequest request,
            final Member member,
            final Post post
    ) {
        final Comment parentComment = commentRepository
                .findParentCommentById(request.getParentCommentId(), member)
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
        if (comment.getMember().equals(receiver)) return;
                comment,
                receiver
        );

        alarmRepository.save(commentReplyAlarm);
    }

    public void deleteComment(
            final Long postId,
            final Long commentId
    ) {
        Comment comment = commentRepository.findCommentById(commentId);
        checkAuthorized(comment);
        commentRepository.deleteCommentById(commentId);
    }

    private void checkAuthorized(final Comment comment) {
        Member member = jwtService.getLoginMember();
        if (comment.isOwner(member)) return;

        Post post = comment.getPost();
        if (post.isOwner(member)) return;

        List<Tag> tagList = tagRepository.findTagsByPost(post);
        for (Tag tag : tagList) {
            if (tag.isMember(member)) return;
        }

        throw new GeneralException(_UNAUTHORIZED);
    }
}
