package com.apps.pochak.login.client;

import com.apps.pochak.login.dto.apple.ApplePublicKeyResponse;
import com.apps.pochak.login.dto.apple.AppleTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@FeignClient(name = "apple-public-key-client", url = "https://appleid.apple.com")
public interface AppleClient {

    @GetMapping(value = "/auth/keys", consumes = MediaType.APPLICATION_JSON_VALUE)
    Optional<ApplePublicKeyResponse> getPublicKey();

    @PostMapping(value = "/auth/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    Optional<AppleTokenResponse> getRefreshToken(
            @RequestParam("client_secret") String clientSecret,
            @RequestParam("code") String authorizationCode,
            @RequestParam("grant_type") String grantType,
            @RequestParam("client_id") String clientId
    );

    @PostMapping(value = "/auth/revoke", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    void revoke(
            @RequestParam("client_secret") String clientSecret,
            @RequestParam("token") String token,
            @RequestParam("client_id") String clientId
    );
}