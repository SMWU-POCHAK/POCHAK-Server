package com.apps.pochak.member.fixture;

import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.SocialType;

public class MemberFixture {

    private static final String PROFILE_IMAGE = "https://avatars.githubusercontent.com/u/163827369?s=200&v=4";

    public static final Member MEMBER1 = new Member(
            1L,
            "member1",
            "1번 회원의 이름",
            "한 줄 소개",
            "aaa@pochak.com",
            PROFILE_IMAGE,
            "REFRESH_TOKEN",
            "SOCIAL_ID",
            SocialType.GOOGLE,
            "SOCIAL_REFRESH_TOKEN"
    );

    public static final Member MEMBER2 = new Member(
            2L,
            "member2",
            "2번 회원의 이름",
            "한 줄 소개",
            "aaa@pochak.com",
            PROFILE_IMAGE,
            "REFRESH_TOKEN",
            "SOCIAL_ID",
            SocialType.APPLE,
            "SOCIAL_REFRESH_TOKEN"
    );

    public static final Member MEMBER3 = new Member(
            3L,
            "member3",
            "3번 회원의 이름",
            "한 줄 소개",
            "aaa@pochak.com",
            PROFILE_IMAGE,
            "REFRESH_TOKEN",
            "SOCIAL_ID",
            SocialType.APPLE,
            "SOCIAL_REFRESH_TOKEN"
    );

    public static final Member BLOCKED_MEMBER = new Member(
            4L,
            "blocked_member",
            "차단된 회원의 이름",
            "한 줄 소개",
            "aaa@pochak.com",
            PROFILE_IMAGE,
            "REFRESH_TOKEN",
            "SOCIAL_ID",
            SocialType.APPLE,
            "SOCIAL_REFRESH_TOKEN"
    );
}
