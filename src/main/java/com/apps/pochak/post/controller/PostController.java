package com.apps.pochak.post.controller;

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
    public ApiResponse<PostElements> getHomeTab(
            @PageableDefault(DEFAULT_PAGING_SIZE) final Pageable pageable
    ) {
        return ApiResponse.onSuccess(postService.getHomeTab(pageable));
    }

    @PostMapping("")
    public ApiResponse<Void> uploadPost(
            @ModelAttribute @Valid final PostUploadRequest request
    ) {
        postService.savePost(request);
        return ApiResponse.of(SUCCESS_UPLOAD_POST);
    }

    @GetMapping("/{postId}")
    public ApiResponse<PostDetailResponse> getPostDetail(
            @PathVariable("postId") final Long postId
    ) {
        return ApiResponse.onSuccess(postService.getPostDetail(postId));
    }

    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deletePost(
            @PathVariable("postId") final Long postId
    ) {
        postService.deletePost(postId);
        return ApiResponse.of(SUCCESS_DELETE_POST);
    }

    @GetMapping("/search")
    public ApiResponse<PostElements> getSearchTab(
            @PageableDefault(DEFAULT_PAGING_SIZE) final Pageable pageable
    ) {
        return ApiResponse.onSuccess(postService.getSearchTab(pageable));
    }
}
