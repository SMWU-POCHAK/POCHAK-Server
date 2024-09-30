package com.apps.pochak.login.client;

import com.apps.pochak.login.dto.google.GoogleMemberResponse;
import com.apps.pochak.login.dto.google.GoogleTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@FeignClient(name = "google-client", url = "https://www.googleapis.com")
public interface GoogleClient {

    @GetMapping(value = "/oauth2/v1/userinfo")
    Optional<GoogleMemberResponse> getPublicKey(
            @RequestParam("access_token") String accessToken
    );

    @PostMapping(value = "/oauth2/v4/token")
    Optional<GoogleTokenResponse> getGoogleAccessToken(
            @RequestParam("grant_type") String grantType,
            @RequestParam("client_id") String clientId,
            @RequestParam("client_secret") String clientSecret,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam("code") String code
    );
}
