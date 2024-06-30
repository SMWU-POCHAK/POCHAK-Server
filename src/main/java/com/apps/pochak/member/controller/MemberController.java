package com.apps.pochak.member.controller;

import com.apps.pochak.global.api_payload.ApiResponse;
import com.apps.pochak.global.api_payload.exception.handler.AppleOAuthException;
import com.apps.pochak.member.dto.request.ProfileUpdateRequest;
import com.apps.pochak.member.dto.response.MemberElements;
import com.apps.pochak.member.service.MemberService;
import com.apps.pochak.post.dto.PostElements;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.apps.pochak.global.Constant.DEFAULT_PAGING_SIZE;

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


    @GetMapping("/search")
    public ApiResponse<MemberElements> searchMember(
            @RequestParam("keyword") final String keyword,
            @PageableDefault(DEFAULT_PAGING_SIZE) final Pageable pageable
    ) {
        return ApiResponse.onSuccess(memberService.search(keyword, pageable));
    }
}
