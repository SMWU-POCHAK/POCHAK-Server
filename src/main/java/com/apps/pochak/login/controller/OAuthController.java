package com.apps.pochak.login.controller;

import com.apps.pochak.auth.Auth;
import com.apps.pochak.auth.MemberOnly;
import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.global.api_payload.ApiResponse;
import com.apps.pochak.login.dto.request.MemberInfoRequest;
import com.apps.pochak.login.dto.response.AccessTokenResponse;
import com.apps.pochak.login.dto.response.OAuthMemberResponse;
import com.apps.pochak.login.service.AppleOAuthService;
import com.apps.pochak.login.service.GoogleOAuthService;
import com.apps.pochak.login.service.OAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.apps.pochak.global.Constant.HEADER_APPLE_AUTHORIZATION_CODE;
import static com.apps.pochak.global.Constant.HEADER_IDENTITY_TOKEN;
import static com.apps.pochak.global.api_payload.code.status.SuccessStatus.SUCCESS_LOG_OUT;
import static com.apps.pochak.global.api_payload.code.status.SuccessStatus.SUCCESS_SIGN_OUT;

@RestController
@RequiredArgsConstructor
public class OAuthController {
    private final OAuthService oAuthService;
    private final AppleOAuthService appleOAuthService;
    private final GoogleOAuthService googleOAuthService;

    @PostMapping(value = "/api/v2/signup")
    public ApiResponse<OAuthMemberResponse> signup(
            @ModelAttribute @Valid final MemberInfoRequest memberInfoRequest
    ) {
        return ApiResponse.onSuccess(oAuthService.signup(memberInfoRequest));
    }

    @PostMapping("/api/v2/refresh")
    public ApiResponse<AccessTokenResponse> refresh() {
        return ApiResponse.onSuccess(oAuthService.reissueAccessToken());
    }

    @PostMapping("/apple/login")
    public ApiResponse<?> appleOAuthRequest(
            @RequestHeader(HEADER_IDENTITY_TOKEN) String idToken,
            @RequestHeader(HEADER_APPLE_AUTHORIZATION_CODE) String authorizationCode
    ) {
        return ApiResponse.onSuccess(appleOAuthService.login(idToken, authorizationCode));
    }

    @GetMapping("/google/login/{accessToken}")
    public ApiResponse<?> googleOAuthRequest(@PathVariable String accessToken) {
        return ApiResponse.onSuccess(googleOAuthService.login(accessToken));
    }

    @GetMapping("/api/v2/logout")
    @MemberOnly
    public ApiResponse<?> logout(
            @Auth Accessor accessor
    ) {
        oAuthService.logout(accessor.getMemberId());
        return ApiResponse.of(SUCCESS_LOG_OUT);
    }

    @DeleteMapping("/api/v2/signout")
    @MemberOnly
    public ApiResponse<?> signout(
            @Auth Accessor accessor
    ) {
        oAuthService.signout(accessor.getMemberId());
        return ApiResponse.of(SUCCESS_SIGN_OUT);
    }
}
