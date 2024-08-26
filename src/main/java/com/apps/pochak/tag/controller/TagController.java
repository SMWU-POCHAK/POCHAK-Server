package com.apps.pochak.tag.controller;

import com.apps.pochak.auth.Auth;
import com.apps.pochak.auth.MemberOnly;
import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.global.api_payload.ApiResponse;
import com.apps.pochak.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/tags")
public class TagController {
    private final TagService tagService;

    @PostMapping("/{tagId}")
    @MemberOnly
    public ApiResponse<Void> approveOrRejectTagRequest(
            @Auth final Accessor accessor,
            @PathVariable("tagId") final Long tagId,
            @RequestParam("isAccept") final Boolean isAccept
    ) {
        return ApiResponse.of(
                tagService.approveOrRejectTagRequest(
                        accessor,
                        tagId,
                        isAccept
                ));
    }
}
