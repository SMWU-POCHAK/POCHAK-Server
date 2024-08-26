package com.apps.pochak.member.fixture;

import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.SocialType;

public class MemberFixture {
    public static final Member MEMBER1 = new Member(
            1L,
            "member1",
            "이름",
            "한 줄 소개",
            "aaa@pochak.com",
            "https://avatars.githubusercontent.com/u/163827369?s=200&v=4",
            "REFRESH_TOKEN",
            "SOCIAL_ID",
            SocialType.GOOGLE,
            "SOCIAL_REFRESH_TOKEN"
    );

    public static final Member MEMBER2 = new Member(
            2L,
            "member2",
            "이름",
            "한 줄 소개",
            "aaa@pochak.com",
            "https://avatars.githubusercontent.com/u/163827369?s=200&v=4",
            "REFRESH_TOKEN",
            "SOCIAL_ID",
            SocialType.APPLE,
            "SOCIAL_REFRESH_TOKEN"
    );
}
