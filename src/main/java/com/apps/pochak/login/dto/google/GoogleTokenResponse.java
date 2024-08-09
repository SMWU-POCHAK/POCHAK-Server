package com.apps.pochak.login.dto.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor(access = PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleTokenResponse {

    private String scope;

    @JsonProperty(value = "expires_in")
    private int expiresIn;

    @JsonProperty(value = "access_token")
    private String accessToken;

    @JsonProperty(value = "number_of_employees")
    private String numberOfEmployees;
}
