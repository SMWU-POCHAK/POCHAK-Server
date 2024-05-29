package com.apps.pochak.follow.domain.repository;

import com.apps.pochak.follow.domain.Follow;
import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.NOT_FOLLOW;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    @Query("select count(f) from Follow f where f.receiver = :member and f.status = 'ACTIVE'")
    long countActiveFollowByReceiver(@Param("member") final Member member);

    @Query("select count(f) from Follow f where f.sender = :member and f.status = 'ACTIVE'")
    long countActiveFollowBySender(@Param("member") final Member member);

    @Query("select count(f.id) > 0 from Follow f " +
            "where f.sender = :sender and f.receiver = :receiver and f.status = 'ACTIVE'")
    boolean existsBySenderAndReceiver(
            @Param("sender") final Member sender,
            @Param("receiver") final Member receiver
    );

    Optional<Follow> findFollowBySenderAndReceiver(final Member sender, final Member receiver);

    default Follow findBySenderAndReceiver(final Member sender, final Member receiver) {
        return findFollowBySenderAndReceiver(sender, receiver).orElseThrow(() -> new GeneralException(NOT_FOLLOW));
    }

    @Query("delete from Follow f " +
            "where (f.sender = :memberA and f.receiver = :memberB) or (f.sender = :memberB and f.receiver = :memberA)")
    void deleteFollowsBetweenMembers(
            @Param("memberA") final Member memberA,
            @Param("memberB") final Member memberB
    );

    @Modifying
    @Query("update Follow follow " +
            "set follow.status = 'DELETED' " +
            "where follow.receiver.id = :memberId or follow.sender.id = :memberId")
    void deleteFollowByMemberId(@Param("memberId") final Long memberId);
}
