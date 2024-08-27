package com.apps.pochak.fcm.controller;

import com.apps.pochak.auth.Auth;
import com.apps.pochak.auth.MemberOnly;
import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.fcm.dto.FCMToken;
import com.apps.pochak.fcm.service.FCMService;
import com.apps.pochak.global.api_payload.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static com.apps.pochak.global.api_payload.code.status.SuccessStatus.SUCCESS_SAVE_TOKEN;

@RestController
@RequiredArgsConstructor
public class FCMController {
    private final FCMService fcmService;

    @PostMapping("/api/v1/fcm")
    @MemberOnly
    public ApiResponse<Void> saveFCMToken(
            @Auth Accessor accessor,
            @RequestBody @Valid FCMToken fcmToken
    ) {
        fcmService.saveToken(accessor, fcmToken);
        return ApiResponse.of(SUCCESS_SAVE_TOKEN);
    }
}
