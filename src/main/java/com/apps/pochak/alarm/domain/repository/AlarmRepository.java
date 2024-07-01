package com.apps.pochak.alarm.domain.repository;

import com.apps.pochak.alarm.domain.Alarm;
import com.apps.pochak.follow.domain.Follow;
import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.like.domain.LikeEntity;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.post.domain.Post;
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

    @Modifying
    @Query("""
            UPDATE Alarm a set a.status = 'DELETED'
            where a.receiver = :member
            or a.tag.post in :postList
            or a.comment.post in :postList
            or a.like.likedPost in :postList
            """)
    void deleteAlarmByMemberId(
            @Param("member") final Member member,
            @Param("postLis") final List<Post> postList
    );

    @Query("select a from Alarm a " +
            "where a.like = :like ")
    List<Alarm> findAlarmByLike(@Param("like") final LikeEntity like);

    List<Alarm> findAlarmByFollow(final Follow follow);

    List<Alarm> findAlarmByTag(final Tag tag);

    List<Alarm> findAlarmByTagIn(final List<Tag> tag);

    // TODO: performance must be checked
    @Query("select a from Alarm a " +
            "left join fetch a.comment c " +
            "   left join fetch c.post " +
            "   left join fetch c.member " +
            "left join fetch a.follow f " +
            "   left join fetch f.sender " +
            "left join fetch a.like l " +
            "   left join fetch l.likedPost " +
            "   left join fetch l.likeMember " +
            "left join fetch a.tag t " +
            "   left join fetch t.post p " +
            "       left join fetch p.owner " +
            "where a.receiver.id = :receiverId " +
            "order by a.createdDate desc ")
    Page<Alarm> getAllAlarm(
            @Param("receiverId") final Long receiverId,
            final Pageable pageable
    );
}
