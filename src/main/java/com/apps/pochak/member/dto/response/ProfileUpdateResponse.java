package com.apps.pochak.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class ProfileUpdateResponse {
    private String name;
    private String handle;
    private String message;
    private String profileImage;

    @Builder
    public static ProfileUpdateResponse of(
            final String name,
            final String handle,
            final String message,
            final String profileImage
    ){
        return new ProfileUpdateResponse(
                name,
                handle,
                message,
                profileImage
        );
    }
}
