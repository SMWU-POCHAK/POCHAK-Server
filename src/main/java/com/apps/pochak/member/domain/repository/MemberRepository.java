package com.apps.pochak.member.domain.repository;

import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.*;

public interface MemberRepository extends JpaRepository<Member, Long> {

    default Member findMemberById(
            final Long id
    ) {
        return findById(id).orElseThrow(() -> new GeneralException(INVALID_MEMBER_HANDLE));
    }

    Optional<Member> findMemberByHandle(final String handle);

    @Query("select m from Member m " +
            "where m.handle = :handle" +
            "   and m not in (select b.blockedMember from Block b where b.blocker = :loginMember) " +
            "   and :loginMember not in (select b.blockedMember from Block b where b.blocker = m) ")
    Optional<Member> findMemberByHandle(
            @Param("handle") final String handle,
            @Param("loginMember") final Member loginMember
    );

    default void checkDuplicateHandle(final String handle) {
        if (findMemberByHandle(handle).isPresent())
            throw new GeneralException(DUPLICATE_HANDLE);
    }

    default Member findByHandleWithoutLogin(final String handle) {
        return findMemberByHandle(handle).orElseThrow(() -> new GeneralException(INVALID_MEMBER_HANDLE));
    }

    default Member findByHandle(
            final String handle,
            final Member loginMember
    ) {
        return findMemberByHandle(handle, loginMember).orElseThrow(() -> new GeneralException(INVALID_MEMBER_HANDLE));
    }

    @Query("select m from Member m " +
            "where m.handle in :handleList " +
            "   and m not in (select b.blockedMember from Block b where b.blocker = :loginMember) " +
            "   and :loginMember not in (select b.blockedMember from Block b where b.blocker = m) ")
    List<Member> findMemberByHandleList(
            @Param("handleList") final List<String> handle,
            @Param("loginMember") final Member loginMember
    );

    Optional<Member> findMemberBySocialId(final String socialId);

    @Query(value = "select m from Member m where m.lastModifiedDate > :nowMinusOneHour ")
    List<Member> findModifiedMemberWithinOneHour(@Param("nowMinusOneHour") final LocalDateTime nowMinusOneHour);

    @Modifying
    @Query("update Member member set member.status = 'DELETED' where member.id = :memberId")
    void deleteMemberByMemberId(@Param("memberId") final Long memberId);

    @Query("select m from Member m " +
            "where (m.handle ilike concat('%', :keyword, '%') or m.name ilike concat('%', :keyword, '%')) " +
            "   and m not in (select b.blockedMember from Block b where b.blocker = :loginMember) " +
            "   and :loginMember not in (select b.blockedMember from Block b where b.blocker = m) ")
    Page<Member> searchByKeyword(
            @Param("keyword") final String keyword,
            @Param("loginMember") final Member loginMember,
            final Pageable pageable
    );
}
