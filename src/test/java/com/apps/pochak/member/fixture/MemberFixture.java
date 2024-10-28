package com.apps.pochak.member.fixture;

import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.SocialType;

public class MemberFixture {

    private static final String PROFILE_IMAGE = "https://avatars.githubusercontent.com/u/163827369?s=200&v=4";

    public static final Member STATIC_MEMBER1 = new Member(
            1L,
            "static_member1",
            "1번 회원의 이름",
            "한 줄 소개",
            "aaa@pochak.com",
            PROFILE_IMAGE,
            "REFRESH_TOKEN",
            "SOCIAL_ID",
            SocialType.GOOGLE,
            "SOCIAL_REFRESH_TOKEN",
            null
    );

    public static final Member STATIC_MEMBER2 = new Member(
            2L,
            "static_member2",
            "2번 회원의 이름",
            "한 줄 소개",
            "aaa@pochak.com",
            PROFILE_IMAGE,
            "REFRESH_TOKEN",
            "SOCIAL_ID",
            SocialType.APPLE,
            "SOCIAL_REFRESH_TOKEN",
            null
    );

    public static final Member WRONG_MEMBER = new Member(
            3L,
            ".wrong_member#",
            "공백이 아닌 열다섯자 이내인 회원의 이름",
            """
            1
            2
            3
            3줄 이내
            """,
            "aaa@pochak.com",
            null,
            "REFRESH_TOKEN",
            "SOCIAL_ID",
            SocialType.APPLE,
            "SOCIAL_REFRESH_TOKEN",
            null
    );

    public static final Member OWNER = Member.signupMember()
            .name("게시물 업로드한 사람")
            .email("aaa@pochak.com")
            .handle("owner")
            .message("한 줄 소개")
            .socialId("SOCIAL_ID")
            .profileImage(PROFILE_IMAGE)
            .refreshToken("REFRESH_TOKEN")
            .socialType(SocialType.GOOGLE)
            .socialRefreshToken("SOCIAL_REFRESH_TOKEN")
            .build();

    public static final Member TAGGED_MEMBER1 = Member.signupMember()
            .name("태그된 사람 1번")
            .email("aaa@pochak.com")
            .handle("tagged_member1")
            .message("한 줄 소개")
            .socialId("SOCIAL_ID")
            .profileImage(PROFILE_IMAGE)
            .refreshToken("REFRESH_TOKEN")
            .socialType(SocialType.GOOGLE)
            .socialRefreshToken("SOCIAL_REFRESH_TOKEN")
            .build();

    public static final Member TAGGED_MEMBER2 = Member.signupMember()
            .name("태그된 사람 2번")
            .email("aaa@pochak.com")
            .handle("tagged_member2")
            .message("한 줄 소개")
            .socialId("SOCIAL_ID")
            .profileImage(PROFILE_IMAGE)
            .refreshToken("REFRESH_TOKEN")
            .socialType(SocialType.GOOGLE)
            .socialRefreshToken("SOCIAL_REFRESH_TOKEN")
            .build();

    public static final Member LOGIN_MEMBER = Member.signupMember()
            .name("로그인한 사람")
            .email("aaa@pochak.com")
            .handle("login_member")
            .message("한 줄 소개")
            .socialId("SOCIAL_ID")
            .profileImage(PROFILE_IMAGE)
            .refreshToken("REFRESH_TOKEN")
            .socialType(SocialType.GOOGLE)
            .socialRefreshToken("SOCIAL_REFRESH_TOKEN")
            .build();

}
