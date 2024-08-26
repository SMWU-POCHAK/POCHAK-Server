package com.apps.pochak.alarm.fixture;

import com.apps.pochak.alarm.domain.CommentAlarm;
import com.apps.pochak.alarm.domain.FollowAlarm;
import com.apps.pochak.alarm.domain.LikeAlarm;
import com.apps.pochak.alarm.domain.TagAlarm;

import static com.apps.pochak.alarm.domain.AlarmType.COMMENT_REPLY;
import static com.apps.pochak.alarm.domain.AlarmType.OWNER_LIKE;
import static com.apps.pochak.comment.fixture.CommentFixture.CHILD_COMMENT;
import static com.apps.pochak.follow.fixture.FollowFixture.RECEIVE_FOLLOW;
import static com.apps.pochak.like.fixture.LikeFixture.LIKE2;
import static com.apps.pochak.member.fixture.MemberFixture.MEMBER1;
import static com.apps.pochak.member.fixture.MemberFixture.MEMBER2;
import static com.apps.pochak.tag.fixture.TagFixture.WAITING_TAG;

public class AlarmFixture {

    public static final CommentAlarm COMMENT_REPLY_ALARM = new CommentAlarm(
            1L,
            CHILD_COMMENT,
            MEMBER1,
            COMMENT_REPLY
    );

    public static final FollowAlarm FOLLOW_ALARM = new FollowAlarm(
            2L,
            RECEIVE_FOLLOW,
            MEMBER1
    );

    public static final LikeAlarm TAGGED_LIKE_ALARM = new LikeAlarm(
            3L,
            LIKE2,
            MEMBER1,
            OWNER_LIKE
    );

    public static final TagAlarm TAG_ALARM = new TagAlarm(
        4L,
            WAITING_TAG,
            MEMBER2,
            MEMBER1
    );
}
