package com.apps.pochak.fcm.controller;

import com.apps.pochak.auth.Auth;
import com.apps.pochak.auth.MemberOnly;
import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.fcm.dto.FCMToken;
import com.apps.pochak.fcm.service.FCMService;
import com.apps.pochak.global.api_payload.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static com.apps.pochak.global.api_payload.code.status.SuccessStatus.SUCCESS_DELETE_TOKEN;
import static com.apps.pochak.global.api_payload.code.status.SuccessStatus.SUCCESS_SAVE_TOKEN;

@RestController
@RequiredArgsConstructor
public class FCMController {
    private final FCMService fcmService;

    @PostMapping("/api/v1/fcm/register")
    @MemberOnly
    public ApiResponse<Void> saveFCMToken(
            @Auth final Accessor accessor,
            @RequestBody @Valid final FCMToken fcmToken
    ) {
        fcmService.saveToken(accessor, fcmToken);
        return ApiResponse.of(SUCCESS_SAVE_TOKEN);
    }

    // Post: /api/v1/fcm
    // TODO: Frontend에서 푸시 알림 요청을 받기

    @DeleteMapping("/api/v1/fcm")
    @MemberOnly
    public ApiResponse<Void> deleteFCMToken(@Auth final Accessor accessor) {
        fcmService.deleteToken(accessor);
        return ApiResponse.of(SUCCESS_DELETE_TOKEN);
    }
}
