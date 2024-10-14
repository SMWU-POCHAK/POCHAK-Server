package com.apps.pochak.global.image;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DirName {
    MEMBER("member"), POST("post");

    private final String dirName;
}
