package com.apps.pochak.block.controller;

import com.apps.pochak.block.service.BlockService;
import com.apps.pochak.global.api_payload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.apps.pochak.global.api_payload.code.status.SuccessStatus.SUCCESS_UPLOAD_REPORT;

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
        return ApiResponse.of(SUCCESS_UPLOAD_REPORT);
    }
}
