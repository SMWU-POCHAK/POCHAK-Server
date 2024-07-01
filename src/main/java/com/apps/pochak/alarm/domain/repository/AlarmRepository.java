package com.apps.pochak.alarm.domain.repository;

import com.apps.pochak.alarm.domain.Alarm;
import com.apps.pochak.follow.domain.Follow;
import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.like.domain.LikeEntity;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.tag.domain.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.INVALID_ALARM_ID;
import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.NOT_YOUR_ALARM;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {

    @Override
    @Query("select a from Alarm a join fetch a.receiver where a.id = :id ")
    Optional<Alarm> findById(@Param("id") final Long id);

    default Alarm findAlarmById(
            final Long id,
            final Member loginMember
    ) {
        final Alarm alarm = findById(id).orElseThrow(() -> new GeneralException(INVALID_ALARM_ID));
        if (!alarm.getReceiver().getId().equals(loginMember.getId())) {
            throw new GeneralException(NOT_YOUR_ALARM);
        }
        return alarm;
    }

    @Query("select a from Alarm a " +
            "where a.receiver.id = :receiverId " +
            "order by a.createdDate desc ")
    Page<Alarm> getAllAlarm(
            @Param("receiverId") final Long receiverId,
            final Pageable pageable
    );

    @Modifying
    @Query("""
            update Alarm a set a.status = 'DELETED'
            where treat(a as LikeAlarm).like = :like
            """)
    void deleteAlarmByLike(@Param("like") final LikeEntity like);

    @Modifying
    @Query("""
            update Alarm a set a.status = 'DELETED'
            where treat(a as FollowAlarm).follow = :follow
            """)
    void deleteAlarmByFollow(@Param("follow") final Follow follow);

    @Modifying
    @Query("""
            update Alarm a set a.status = 'DELETED'
            where treat(a as TagAlarm).tag = :tag
            """)
    void deleteAlarmByTag(@Param("tag") final Tag tag);

    @Modifying
    @Query("""
            update Alarm a set a.status = 'DELETED'
            where treat(a as TagAlarm).tag in :tagList
            """)
    void deleteAlarmByTagList(@Param("tagList") final List<Tag> tag);
}
