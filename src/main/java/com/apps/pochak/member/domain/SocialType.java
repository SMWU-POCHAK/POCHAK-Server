package com.apps.pochak.member.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum SocialType {
    GOOGLE("google"), APPLE("apple");

    private final String code;

    public static SocialType of(String code) {
        return Arrays.stream(SocialType.values())
                .filter(r -> r.getCode().equals(code.toLowerCase()))
                .findAny()
                .orElse(null);
    }
}
