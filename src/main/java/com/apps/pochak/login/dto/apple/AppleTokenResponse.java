package com.apps.pochak.login.dto.apple;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppleTokenResponse {
    private String error;

    @JsonProperty(value = "id_token")
    private String idToken;

    @JsonProperty(value = "expires_in")
    private String expiresIn;

    @JsonProperty(value = "token_type")
    private String tokenType;

    @JsonProperty(value = "access_token")
    private String accessToken;

    @JsonProperty(value = "refresh_token")
    private String refreshToken;
}
