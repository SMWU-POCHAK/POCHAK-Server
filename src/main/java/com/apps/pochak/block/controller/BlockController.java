package com.apps.pochak.block.controller;

import com.apps.pochak.block.dto.response.BlockElements;
import com.apps.pochak.block.service.BlockService;
import com.apps.pochak.global.api_payload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import static com.apps.pochak.global.Constant.DEFAULT_PAGING_SIZE;
import static com.apps.pochak.global.api_payload.code.status.SuccessStatus.SUCCESS_BLOCK_MEMBER;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v2/members/{handle}")
public class BlockController {
    private final BlockService blockService;

    @PostMapping("/block")
    public ApiResponse<Void> blockMember(
            @PathVariable("handle") final String handle
    ) {
        blockService.blockMember(handle);
        return ApiResponse.of(SUCCESS_BLOCK_MEMBER);
    }

    @GetMapping("/block")
    public ApiResponse<BlockElements> getBlockedMembers(
            @PathVariable("handle") final String handle,
            @PageableDefault(DEFAULT_PAGING_SIZE) final Pageable pageable
    ) {
        return ApiResponse.onSuccess(blockService.getBlockedMember(handle, pageable));
    }
}
