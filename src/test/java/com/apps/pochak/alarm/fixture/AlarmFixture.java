package com.apps.pochak.alarm.fixture;

import com.apps.pochak.alarm.domain.CommentAlarm;
import com.apps.pochak.alarm.domain.FollowAlarm;
import com.apps.pochak.alarm.domain.LikeAlarm;
import com.apps.pochak.alarm.domain.TagAlarm;

import static com.apps.pochak.alarm.domain.AlarmType.COMMENT_REPLY;
import static com.apps.pochak.alarm.domain.AlarmType.OWNER_LIKE;
import static com.apps.pochak.comment.fixture.CommentFixture.STATIC_CHILD_COMMENT;
import static com.apps.pochak.follow.fixture.FollowFixture.STATIC_RECEIVE_FOLLOW;
import static com.apps.pochak.like.fixture.LikeFixture.STATIC_LIKE2;
import static com.apps.pochak.member.fixture.MemberFixture.STATIC_MEMBER1;
import static com.apps.pochak.member.fixture.MemberFixture.STATIC_MEMBER2;
import static com.apps.pochak.tag.fixture.TagFixture.STATIC_WAITING_TAG;

public class AlarmFixture {

    public static final CommentAlarm STATIC_COMMENT_REPLY_ALARM = new CommentAlarm(
            1L,
            STATIC_CHILD_COMMENT,
            STATIC_MEMBER1,
            COMMENT_REPLY
    );

    public static final FollowAlarm STATIC_FOLLOW_ALARM = new FollowAlarm(
            2L,
            STATIC_RECEIVE_FOLLOW,
            STATIC_MEMBER1
    );

    public static final LikeAlarm STATIC_TAGGED_LIKE_ALARM = new LikeAlarm(
            3L,
            STATIC_LIKE2,
            STATIC_MEMBER1,
            OWNER_LIKE
    );

    public static final TagAlarm STATIC_TAG_ALARM = new TagAlarm(
            4L,
            STATIC_WAITING_TAG,
            STATIC_MEMBER2,
            STATIC_MEMBER1
    );
}
