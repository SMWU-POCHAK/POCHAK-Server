package com.apps.pochak.member.domain.repository;

import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.dto.response.MemberElement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomMemberRepository extends JpaRepository<Member, Long> {
    @Query("select  " +
            "new com.apps.pochak.member.dto.response.MemberElement(" +
                "m.profileImage, " +
                "m.handle, " +
                "m.name, " +
                "(case when m.id <> :loginMemberId then (fo.id is not null) else nullif(m.id, :loginMemberId) end) " +
            ") " +
            "from Follow f " +
            "join Member m on (f.sender = m and f.receiver = :member " +
            "                       and m not in (select b.blockedMember from Block b where b.blocker.id = :loginMemberId) " +
            "                       and :loginMemberId not in (select b.blockedMember.id from Block b where b.blocker = m)) " +
            "left join Follow fo on (fo.receiver = f.sender and fo.sender.id = :loginMemberId and fo.status = 'ACTIVE') " +
            "where f.status = 'ACTIVE' " +
            "order by f.lastModifiedDate desc ")
    Page<MemberElement> findFollowersAndIsFollow(
            @Param("member") final Member member,
            @Param("loginMemberId") final Long loginMemberId,
            final Pageable pageable
    );

    @Query("select  " +
            "new com.apps.pochak.member.dto.response.MemberElement(" +
                "m.profileImage, " +
                "m.handle, " +
                "m.name, " +
                "(case when m.id <> :loginMemberId then (fo.id is not null) else nullif(m.id, :loginMemberId) end) " +
            ") " +
            "from Follow f " +
            "join Member m on (f.receiver = m and f.sender = :member " +
            "                       and m not in (select b.blockedMember from Block b where b.blocker.id = :loginMemberId) " +
            "                       and :loginMemberId not in (select b.blockedMember.id from Block b where b.blocker = m)) " +
            "left join Follow fo on (fo.receiver = f.receiver and fo.sender.id = :loginMemberId and fo.status = 'ACTIVE') " +
            "where f.status = 'ACTIVE' " +
            "order by f.lastModifiedDate desc ")
    Page<MemberElement> findFollowingsAndIsFollow(
            @Param("member") final Member member,
            @Param("loginMemberId") final Long loginMemberId,
            final Pageable pageable
    );
}
