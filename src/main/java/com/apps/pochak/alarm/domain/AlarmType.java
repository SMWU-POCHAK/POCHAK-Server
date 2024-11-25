package com.apps.pochak.alarm.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AlarmType {
    OWNER_COMMENT("%s님이 댓글을 달았습니다.", "%s", "%s"),
    TAGGED_COMMENT("내가 포착된 게시물에 %s님이 댓글을 달았습니다.", "%s", "%s"),
    COMMENT_REPLY("나의 댓글에 %s님이 답글을 달았습니다.", "%s", "%s"),
    FOLLOW("이제 친구를 포착해보세요!", "%s님이 회원님을 팔로우하였습니다.", "%s"),
    TAGGED_LIKE("새로운 반응을 확인하세요!", "내가 포착된 게시물에 %s님이 좋아요를 눌렀습니다.", "%s"),
    OWNER_LIKE("새로운 반응을 확인하세요!", "내 게시물에 %s님이 좋아요를 눌렀습니다.", "%s"),
    TAG_APPROVAL("포착된 사진을 확인해보세요!", "%s님이 회원님을 포착했습니다.", "%s");

    private final String title;
    private final String body;
    private final String image;
}
