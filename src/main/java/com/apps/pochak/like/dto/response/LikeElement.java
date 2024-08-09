package com.apps.pochak.like.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LikeElement {
    private Long memberId;
    private String handle;
    private String profileImage;
    private String name;
    private Boolean follow;
}
