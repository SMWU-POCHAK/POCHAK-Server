package com.apps.pochak.comment.controller;

import com.apps.pochak.auth.Auth;
import com.apps.pochak.auth.MemberOnly;
import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.comment.dto.request.CommentUploadRequest;
import com.apps.pochak.comment.dto.response.CommentElements;
import com.apps.pochak.comment.dto.response.ParentCommentElement;
import com.apps.pochak.comment.service.CommentService;
import com.apps.pochak.global.api_payload.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import static com.apps.pochak.global.Constant.DEFAULT_PAGING_SIZE;
import static com.apps.pochak.global.api_payload.code.status.SuccessStatus.SUCCESS_DELETE_COMMENT;
import static com.apps.pochak.global.api_payload.code.status.SuccessStatus.SUCCESS_UPLOAD_COMMENT;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v2/posts/{postId}/comments")
public class CommentController {
    private final CommentService commentService;

    @GetMapping("")
    @MemberOnly
    public ApiResponse<CommentElements> getComments(
            @Auth final Accessor accessor,
            @PathVariable("postId") final Long postId,
            @PageableDefault(DEFAULT_PAGING_SIZE) final Pageable pageable
    ) {
        return ApiResponse.onSuccess(
                commentService.getComments(
                        accessor,
                        postId,
                        pageable
                )
        );
    }

    @GetMapping("{parentCommentId}")
    @MemberOnly
    public ApiResponse<ParentCommentElement> getChildComments(
            @Auth final Accessor accessor,
            @PathVariable("postId") final Long postId,
            @PathVariable("parentCommentId") final Long parentCommentId,
            @PageableDefault(DEFAULT_PAGING_SIZE) final Pageable pageable
    ) {
        return ApiResponse.onSuccess(
                commentService.getChildCommentsByParentCommentId(
                        accessor,
                        postId,
                        parentCommentId,
                        pageable
                )
        );
    }

    @PostMapping("")
    @MemberOnly
    public ApiResponse<Void> saveComment(
            @Auth final Accessor accessor,
            @PathVariable("postId") final Long postId,
            @RequestBody @Valid final CommentUploadRequest request
    ) {
        commentService.saveComment(
                accessor,
                postId,
                request
        );
        return ApiResponse.of(SUCCESS_UPLOAD_COMMENT);
    }

    @DeleteMapping("")
    @MemberOnly
    public ApiResponse<Void> deleteComment(
            @Auth final Accessor accessor,
            @PathVariable("postId") final Long postId,
            @RequestParam("commentId") final Long commentId
    ) {
        commentService.deleteComment(
                accessor,
                postId,
                commentId
        );
        return ApiResponse.of(SUCCESS_DELETE_COMMENT);
    }
}
