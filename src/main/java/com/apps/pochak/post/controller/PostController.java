package com.apps.pochak.post.controller;

import com.apps.pochak.auth.Auth;
import com.apps.pochak.auth.MemberOnly;
import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.global.api_payload.ApiResponse;
import com.apps.pochak.post.dto.PostElements;
import com.apps.pochak.post.dto.request.PostUploadRequest;
import com.apps.pochak.post.dto.response.PostDetailResponse;
import com.apps.pochak.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import static com.apps.pochak.global.Constant.DEFAULT_PAGING_SIZE;
import static com.apps.pochak.global.api_payload.code.status.SuccessStatus.SUCCESS_DELETE_POST;
import static com.apps.pochak.global.api_payload.code.status.SuccessStatus.SUCCESS_UPLOAD_POST;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v2/posts")
public class PostController {
    private final PostService postService;

    @GetMapping("")
    @MemberOnly
    public ApiResponse<PostElements> getHomeTab(
            @Auth final Accessor accessor,
            @PageableDefault(DEFAULT_PAGING_SIZE) final Pageable pageable
    ) {
        return ApiResponse.onSuccess(postService.getHomeTab(accessor, pageable));
    }

    @PostMapping("")
    @MemberOnly
    public ApiResponse<Void> uploadPost(
            @Auth final Accessor accessor,
            @ModelAttribute @Valid final PostUploadRequest request
    ) {
        postService.savePost(accessor, request);
        return ApiResponse.of(SUCCESS_UPLOAD_POST);
    }

    @GetMapping("/{postId}")
    @MemberOnly
    public ApiResponse<PostDetailResponse> getPostDetail(
            @Auth final Accessor accessor,
            @PathVariable("postId") final Long postId
    ) {
        return ApiResponse.onSuccess(postService.getPostDetail(accessor, postId));
    }

    @DeleteMapping("/{postId}")
    @MemberOnly
    public ApiResponse<Void> deletePost(
            @Auth final Accessor accessor,
            @PathVariable("postId") final Long postId
    ) {
        postService.deletePost(accessor, postId);
        return ApiResponse.of(SUCCESS_DELETE_POST);
    }

    @GetMapping("/search")
    @MemberOnly
    public ApiResponse<PostElements> getSearchTab(
            @Auth final Accessor accessor,
            @PageableDefault(DEFAULT_PAGING_SIZE) final Pageable pageable
    ) {
        return ApiResponse.onSuccess(postService.getSearchTab(accessor, pageable));
    }
}
