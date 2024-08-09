package com.apps.pochak.follow.controller;

import com.apps.pochak.follow.service.FollowService;
import com.apps.pochak.global.api_payload.ApiResponse;
import com.apps.pochak.member.dto.response.MemberElements;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import static com.apps.pochak.global.Constant.DEFAULT_PAGING_SIZE;
import static com.apps.pochak.global.api_payload.code.status.SuccessStatus.SUCCESS_DELETE_FOLLOWER;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v2/members")
public class FollowController {
    private final FollowService followService;

    @GetMapping("/{handle}/following")
    public ApiResponse<MemberElements> getFollowings(
            @PathVariable("handle") final String handle,
            @PageableDefault(DEFAULT_PAGING_SIZE) final Pageable pageable
    ) {
        return ApiResponse.onSuccess(followService.getFollowings(handle, pageable));
    }

    @GetMapping("/{handle}/follower")
    public ApiResponse<MemberElements> getFollowers(
            @PathVariable("handle") final String handle,
            @PageableDefault(DEFAULT_PAGING_SIZE) final Pageable pageable
    ) {
        return ApiResponse.onSuccess(followService.getFollowers(handle, pageable));
    }


    @PostMapping("/{handle}/follow")
    public ApiResponse<Void> followMember(
            @PathVariable("handle") final String handle
    ) {
        return ApiResponse.of(followService.follow(handle));
    }

    @DeleteMapping("/{handle}/follower")
    public ApiResponse<Void> deleteFollower(
            @PathVariable("handle") final String handle,
            @RequestParam("followerHandle") final String followerHandle
    ) {
        followService.deleteFollower(handle, followerHandle);
        return ApiResponse.of(SUCCESS_DELETE_FOLLOWER);
    }
}
