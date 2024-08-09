package com.apps.pochak.alarm.controller;

import com.apps.pochak.alarm.dto.response.AlarmElements;
import com.apps.pochak.alarm.service.AlarmService;
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
    public ApiResponse<AlarmElements> getAllAlarms(
            @PageableDefault(DEFAULT_PAGING_SIZE) final Pageable pageable
    ) {
        return ApiResponse.onSuccess(alarmService.getAllAlarms(pageable));
    }

    @GetMapping("/{alarmId}")
    public ApiResponse<PostPreviewResponse> getPreviewPost(
            @PathVariable("alarmId") Long alarmId
    ) {
        return ApiResponse.onSuccess(postService.getPreviewPost(alarmId));
    }

    @PostMapping("/{alarmId}")
    public ApiResponse<Void> checkAlarm(
            @PathVariable("alarmId") Long alarmId
    ) {
        return ApiResponse.of(alarmService.checkAlarm(alarmId));
    }
}
