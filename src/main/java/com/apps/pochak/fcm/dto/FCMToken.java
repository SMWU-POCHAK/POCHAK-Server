package com.apps.pochak.fcm.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class FCMToken {
    @NotNull(message = "FCM 토큰은 필수로 전달해야 합니다.")
    private String token;
}
