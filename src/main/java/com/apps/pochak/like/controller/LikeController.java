package com.apps.pochak.like.controller;

import com.apps.pochak.auth.Auth;
import com.apps.pochak.auth.MemberOnly;
import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.global.api_payload.ApiResponse;
import com.apps.pochak.like.dto.response.LikeElements;
import com.apps.pochak.like.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.apps.pochak.global.api_payload.code.status.SuccessStatus.SUCCESS_LIKE;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v2/posts/{postId}/like")
public class LikeController {
    private final LikeService likeService;

    // TODO: Paging
    @GetMapping("")
    @MemberOnly
    public ApiResponse<LikeElements> getLikeMembers(
            @Auth final Accessor accessor,
            @PathVariable("postId") final Long postId
    ) {
        return ApiResponse.onSuccess(likeService.getMemberLikedPost(accessor, postId));
    }

    @PostMapping("")
    @MemberOnly
    public ApiResponse<Void> likePost(
            @Auth final Accessor accessor,
            @PathVariable("postId") final Long postId
    ) {
        likeService.likePost(accessor, postId);
        return ApiResponse.of(SUCCESS_LIKE);
    }
}
