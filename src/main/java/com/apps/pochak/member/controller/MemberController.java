package com.apps.pochak.member.controller;

import com.apps.pochak.global.api_payload.ApiResponse;
import com.apps.pochak.member.service.MemberService;
import com.apps.pochak.post.dto.PostElements;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v2/members")
public class MemberController {
    private final MemberService memberService;
    public static final int PROFILE_PAGING_SIZE = 12;

    @GetMapping("/{handle}")
    public ApiResponse<?> getProfileDetail(
            @PathVariable("handle") final String handle,
            @PageableDefault(PROFILE_PAGING_SIZE) final Pageable pageable
    ) {
        if (pageable.getPageNumber() == 0)
            return ApiResponse.onSuccess(memberService.getProfileDetail(handle, pageable));
        else
            return ApiResponse.onSuccess(memberService.getTaggedPosts(handle, pageable));
    }

    @GetMapping("/{handle}/upload")
    public ApiResponse<PostElements> getUploadPosts(
            @PathVariable("handle") final String handle,
            @PageableDefault(PROFILE_PAGING_SIZE) final Pageable pageable
    ) {
        return ApiResponse.onSuccess(memberService.getUploadPosts(handle, pageable));
    }
}
