package com.apps.pochak.login.dto.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor(access = PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleMemberResponse {

    public String id;
    public String name;
    public String email;
}
