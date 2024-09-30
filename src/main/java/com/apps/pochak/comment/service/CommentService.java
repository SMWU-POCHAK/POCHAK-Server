package com.apps.pochak.comment.service;

import com.apps.pochak.alarm.service.CommentAlarmService;
import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.comment.domain.Comment;
import com.apps.pochak.comment.domain.repository.CommentRepository;
import com.apps.pochak.comment.dto.request.CommentUploadRequest;
import com.apps.pochak.comment.dto.response.CommentElements;
import com.apps.pochak.comment.dto.response.ParentCommentElement;
import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.login.provider.JwtProvider;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.post.domain.repository.PostRepository;
import com.apps.pochak.tag.domain.Tag;
import com.apps.pochak.tag.domain.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final MemberRepository memberRepository;

    private final CommentAlarmService commentAlarmService;

    @Transactional(readOnly = true)
    public CommentElements getComments(
            final Accessor accessor,
            final Long postId,
            final Pageable pageable
    ) {
        final Member loginMember = memberRepository.findMemberById(accessor.getMemberId());
        final Post post = postRepository.findPublicPostById(postId);
        final Page<Comment> commentList = commentRepository.findParentCommentByPost(post, loginMember, pageable);
        return new CommentElements(loginMember, commentList);
    }

    @Transactional(readOnly = true)
    public ParentCommentElement getChildCommentsByParentCommentId(
            final Accessor accessor,
            final Long postId,
            final Long parentCommentId,
            final Pageable pageable
    ) {
        final Member loginMember = memberRepository.findMemberById(accessor.getMemberId());
        final Comment comment = commentRepository.findParentCommentById(parentCommentId, loginMember)
                .orElseThrow(() -> new GeneralException(INVALID_POST_ID));
        return new ParentCommentElement(comment, toPageRequest(pageable));
    }

    public void saveComment(
            final Accessor accessor,
            final Long postId,
            final CommentUploadRequest request
    ) {
        final Member loginMember = memberRepository.findMemberById(accessor.getMemberId());
        final Post post = postRepository.findPublicPostById(postId);

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
            final Member loginMember,
            final Post post
    ) {
        final Comment parentComment = commentRepository
                .findParentCommentById(request.getParentCommentId(), loginMember)
                .orElseThrow(() -> new GeneralException(INVALID_COMMENT_ID));
        final Comment comment = commentRepository.save(
                request.toEntity(
                        loginMember,
                        post,
                        parentComment
                )
        );
        final Member parentCommentWriter = parentComment.getMember();
        commentAlarmService.sendChildCommentAlarm(
                comment,
                post.getOwner(),
                parentCommentWriter
        );
    }

    private void saveParentComment(
            final CommentUploadRequest request,
            final Member loginMember,
            final Post post
    ) {
        final Comment comment = commentRepository.save(request.toEntity(loginMember, post));
        commentAlarmService.sendParentCommentAlarm(
                comment,
                post.getOwner()
        );
    }

    public void deleteComment(
            final Accessor accessor,
            final Long postId,
            final Long commentId
    ) {
        Comment comment = commentRepository.findCommentById(commentId);
        checkAuthorized(accessor, comment);
        commentRepository.deleteCommentById(commentId);
        commentAlarmService.deleteAlarmByComment(comment);
    }

    private void checkAuthorized(
            final Accessor accessor,
            final Comment comment
    ) {
        Member member = memberRepository.findMemberById(accessor.getMemberId());
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
