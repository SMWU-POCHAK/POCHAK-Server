package com.apps.pochak.member.controller;

import com.apps.pochak.auth.Auth;
import com.apps.pochak.auth.MemberOnly;
import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.global.api_payload.ApiResponse;
import com.apps.pochak.member.dto.request.ProfileUpdateRequest;
import com.apps.pochak.member.dto.response.MemberElements;
import com.apps.pochak.member.dto.response.ProfileUpdateResponse;
import com.apps.pochak.member.service.MemberService;
import com.apps.pochak.post.dto.PostElements;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import static com.apps.pochak.global.Constant.DEFAULT_PAGING_SIZE;
import static com.apps.pochak.global.api_payload.code.status.SuccessStatus.VALID_HANDLE;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v2/members")
public class MemberController {
    private final MemberService memberService;
    public static final int PROFILE_PAGING_SIZE = 12;

    @GetMapping("/{handle}")
    @MemberOnly
    public ApiResponse<?> getProfileDetail(
            @Auth final Accessor accessor,
            @PathVariable("handle") final String handle,
            @PageableDefault(PROFILE_PAGING_SIZE) final Pageable pageable
    ) {
        if (pageable.getPageNumber() == 0)
            return ApiResponse.onSuccess(
                    memberService.getProfileDetail(
                            accessor,
                            handle,
                            pageable
                    )
            );
        else
            return ApiResponse.onSuccess(
                    memberService.getTaggedPosts(
                            accessor,
                            handle,
                            pageable
                    )
            );
    }

    @GetMapping("/{handle}/upload")
    @MemberOnly
    public ApiResponse<PostElements> getUploadPosts(
            @Auth final Accessor accessor,
            @PathVariable("handle") final String handle,
            @PageableDefault(PROFILE_PAGING_SIZE) final Pageable pageable
    ) {
        return ApiResponse.onSuccess(
                memberService.getUploadPosts(
                        accessor,
                        handle,
                        pageable
                )
        );
    }

    @PutMapping("/{handle}")
    @MemberOnly
    public ApiResponse<ProfileUpdateResponse> updateProfile(
            @Auth final Accessor accessor,
            @PathVariable("handle") final String handle,
            @ModelAttribute final ProfileUpdateRequest profileUpdateRequest) {
        return ApiResponse.onSuccess(
                memberService.updateProfile(
                        accessor,
                        handle,
                        profileUpdateRequest
                ));
    }

    @GetMapping("/search")
    @MemberOnly
    public ApiResponse<MemberElements> searchMember(
            @Auth final Accessor accessor,
            @RequestParam("keyword") final String keyword,
            @PageableDefault(DEFAULT_PAGING_SIZE) final Pageable pageable
    ) {
        return ApiResponse.onSuccess(
                memberService.search(
                        accessor,
                        keyword,
                        pageable
                )
        );
    }

    @GetMapping("/duplicate")
    public ApiResponse<Void> checkDuplicate(
            @RequestParam("handle") final String handle
    ) {
        memberService.checkDuplicate(handle);
        return ApiResponse.of(VALID_HANDLE);
    }
}
