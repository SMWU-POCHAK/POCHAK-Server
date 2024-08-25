package com.apps.pochak.block.controller;

import com.apps.pochak.auth.Auth;
import com.apps.pochak.auth.MemberOnly;
import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.block.dto.response.BlockElements;
import com.apps.pochak.block.service.BlockService;
import com.apps.pochak.global.api_payload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import static com.apps.pochak.global.Constant.DEFAULT_PAGING_SIZE;
import static com.apps.pochak.global.api_payload.code.status.SuccessStatus.SUCCESS_BLOCK_MEMBER;
import static com.apps.pochak.global.api_payload.code.status.SuccessStatus.SUCCESS_CANCEL_BLOCK;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v2/members/{handle}")
public class BlockController {
    private final BlockService blockService;

    @PostMapping("/block")
    @MemberOnly
    public ApiResponse<Void> blockMember(
            @Auth final Accessor accessor,
            @PathVariable("handle") final String handle
    ) {
        blockService.blockMember(accessor, handle);
        return ApiResponse.of(SUCCESS_BLOCK_MEMBER);
    }

    @GetMapping("/block")
    @MemberOnly
    public ApiResponse<BlockElements> getBlockedMembers(
            @Auth final Accessor accessor,
            @PathVariable("handle") final String handle,
            @PageableDefault(DEFAULT_PAGING_SIZE) final Pageable pageable
    ) {
        return ApiResponse.onSuccess(blockService.getBlockedMember(
                accessor,
                handle,
                pageable
        ));
    }

    @DeleteMapping("/block")
    @MemberOnly
    public ApiResponse<Void> cancelBlock(
            @Auth final Accessor accessor,
            @PathVariable("handle") final String handle,
            @RequestParam("blockedMemberHandle") final String blockedMemberHandle
    ) {
        blockService.cancelBlock(
                accessor,
                handle,
                blockedMemberHandle
        );
        return ApiResponse.of(SUCCESS_CANCEL_BLOCK);
    }
}
