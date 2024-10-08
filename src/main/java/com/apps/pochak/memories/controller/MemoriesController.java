package com.apps.pochak.memories.controller;

import com.apps.pochak.auth.Auth;
import com.apps.pochak.auth.MemberOnly;
import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.global.api_payload.ApiResponse;
import com.apps.pochak.memories.dto.response.MemoriesPostResponse;
import com.apps.pochak.memories.dto.response.MemoriesPreviewResponse;
import com.apps.pochak.memories.service.MemoriesService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.apps.pochak.global.Constant.DEFAULT_PAGING_SIZE;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/memories")
public class MemoriesController {

    private final MemoriesService memoriesService;

    @GetMapping("{handle}")
    @MemberOnly
    public ApiResponse<MemoriesPreviewResponse> getMemories(
            @Auth final Accessor accessor,
            @PathVariable("handle") final String handle
    ) {
        return ApiResponse.onSuccess(memoriesService.getMemories(accessor, handle));
    }

    @GetMapping("{handle}/pochak")
    @MemberOnly
    public ApiResponse<MemoriesPostResponse> getPochak(
            @Auth final Accessor accessor,
            @PathVariable("handle") final String handle,
            @PageableDefault(DEFAULT_PAGING_SIZE) final Pageable pageable
    ) {
        return ApiResponse.onSuccess(memoriesService.getPochak(accessor, handle, pageable));
    }

    @GetMapping("{handle}/pochaked")
    @MemberOnly
    public ApiResponse<MemoriesPostResponse> getPochaked(
            @Auth final Accessor accessor,
            @PathVariable("handle") final String handle,
            @PageableDefault(DEFAULT_PAGING_SIZE) final Pageable pageable
    ) {
        return ApiResponse.onSuccess(memoriesService.getPochaked(accessor, handle, pageable));
    }

    @GetMapping("{handle}/bonded")
    @MemberOnly
    public ApiResponse<MemoriesPostResponse> getBonded(
            @Auth final Accessor accessor,
            @PathVariable("handle") final String handle,
            @PageableDefault(DEFAULT_PAGING_SIZE) final Pageable pageable
    ) {
        return ApiResponse.onSuccess(memoriesService.getBonded(accessor, handle, pageable));
    }

}
