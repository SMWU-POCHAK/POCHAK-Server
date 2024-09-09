package com.apps.pochak.alarm.controller;

import com.apps.pochak.alarm.dto.response.AlarmElements;
import com.apps.pochak.alarm.service.AlarmService;
import com.apps.pochak.auth.Auth;
import com.apps.pochak.auth.MemberOnly;
import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.global.api_payload.ApiResponse;
import com.apps.pochak.post.dto.response.PostPreviewResponse;
import com.apps.pochak.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import static com.apps.pochak.global.Constant.DEFAULT_PAGING_SIZE;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/alarms")
public class AlarmController {
    private final AlarmService alarmService;
    private final PostService postService;

    @GetMapping("")
    @MemberOnly
    public ApiResponse<AlarmElements> getAllAlarms(
            @Auth final Accessor accessor,
            @PageableDefault(DEFAULT_PAGING_SIZE) final Pageable pageable
    ) {
        return ApiResponse.onSuccess(alarmService.getAllAlarms(accessor, pageable));
    }

    @GetMapping("/{alarmId}")
    @MemberOnly
    public ApiResponse<PostPreviewResponse> getPreviewPost(
            @Auth final Accessor accessor,
            @PathVariable("alarmId") Long alarmId
    ) {
        return ApiResponse.onSuccess(postService.getPreviewPost(accessor, alarmId));
    }

    @PostMapping("/{alarmId}")
    @MemberOnly
    public ApiResponse<Void> checkAlarm(
            @Auth final Accessor accessor,
            @PathVariable("alarmId") Long alarmId
    ) {
        return ApiResponse.of(alarmService.checkAlarm(accessor, alarmId));
    }
}
