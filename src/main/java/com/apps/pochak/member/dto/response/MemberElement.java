package com.apps.pochak.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberElement {
    private String profileImage;
    private String handle;
    private String name;
    private Boolean isFollow;

    public MemberElement(
            final String profileImage,
            final String handle,
            final String name,
            final Long isFollow
    ) {
        this.profileImage = profileImage;
        this.handle = handle;
        this.name = name;
        this.isFollow = convert(isFollow);
    }

    private Boolean convert(Long value) {
        if (value == null) {
            return null;
        } else {
            return value != 0;
        }
    }
}
