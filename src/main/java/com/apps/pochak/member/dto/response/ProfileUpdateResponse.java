package com.apps.pochak.member.dto.response;

import com.apps.pochak.member.dto.request.ProfileUpdateRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@Getter
@RequiredArgsConstructor(access = PRIVATE)
public class ProfileUpdateResponse {
    private final String name;
    private final String handle;
    private final String message;
    private final String profileImage;

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
