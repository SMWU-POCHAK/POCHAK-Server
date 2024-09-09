package com.apps.pochak.follow.controller;

import com.apps.pochak.auth.Auth;
import com.apps.pochak.auth.MemberOnly;
import com.apps.pochak.auth.domain.Accessor;
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
    @MemberOnly
    public ApiResponse<MemberElements> getFollowings(
            @Auth final Accessor accessor,
            @PathVariable("handle") final String handle,
            @PageableDefault(DEFAULT_PAGING_SIZE) final Pageable pageable
    ) {
        return ApiResponse.onSuccess(
                followService.getFollowings(
                        accessor,
                        handle,
                        pageable
                )
        );
    }

    @GetMapping("/{handle}/follower")
    @MemberOnly
    public ApiResponse<MemberElements> getFollowers(
            @Auth final Accessor accessor,
            @PathVariable("handle") final String handle,
            @PageableDefault(DEFAULT_PAGING_SIZE) final Pageable pageable
    ) {
        return ApiResponse.onSuccess(
                followService.getFollowers(
                        accessor,
                        handle,
                        pageable
                )
        );
    }


    @PostMapping("/{handle}/follow")
    @MemberOnly
    public ApiResponse<Void> followMember(
            @Auth final Accessor accessor,
            @PathVariable("handle") final String handle
    ) {
        return ApiResponse.of(followService.follow(accessor, handle));
    }

    @DeleteMapping("/{handle}/follower")
    @MemberOnly
    public ApiResponse<Void> deleteFollower(
            @Auth final Accessor accessor,
            @PathVariable("handle") final String handle,
            @RequestParam("followerHandle") final String followerHandle
    ) {
        followService.deleteFollower(
                accessor,
                handle,
                followerHandle
        );
        return ApiResponse.of(SUCCESS_DELETE_FOLLOWER);
    }
}
