package com.apps.pochak.member.domain.repository;

import com.apps.pochak.follow.domain.Follow;
import com.apps.pochak.follow.domain.QFollow;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.dto.response.MemberElement;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

import static com.apps.pochak.block.domain.QBlock.block;
import static com.apps.pochak.follow.domain.QFollow.follow;
import static com.apps.pochak.global.BaseEntityStatus.ACTIVE;
import static com.apps.pochak.member.domain.QMember.member;

@Repository
@RequiredArgsConstructor
public class MemberCustomRepository {
    private final JPAQueryFactory query;
    public static final QFollow f = new QFollow("f");

    public Page<MemberElement> findFollowersOfMemberAndIsFollow(
            final Long memberId,
            final Long loginMemberId,
            final Pageable pageable
    ) {
        List<MemberElement> memberElementList =
                findFollowersOfMemberAndIsFollow(memberId, loginMemberId)
                        .orderBy(follow.lastModifiedDate.desc())
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .fetch()
                        .stream()
                        .map(
                                tuple -> new MemberElement(
                                        tuple.get(member),
                                        getFollowStatus(tuple.get(member), tuple.get(f), loginMemberId)
                                )
                        ).collect(Collectors.toList());

        JPAQuery<Long> countQuery = query.select(member.count())
                .from(member)
                .where(member
                        .in(
                                findFollowersOfMemberAndIsFollow(memberId, loginMemberId).select(member)
                        ));

        return PageableExecutionUtils.getPage(memberElementList, pageable, countQuery::fetchOne);
    }

    private Boolean getFollowStatus(
            final Member member,
            final Follow follow,
            final Long loginMemberId
    ) {
        if (member.getId().equals(loginMemberId)) return null;
        else return follow != null;
    }

    private JPQLQuery<Tuple> findFollowersOfMemberAndIsFollow(
            final Long memberId,
            final Long loginMemberId
    ) {
        return query.select(member, follow, f)
                .from(member)
                .join(follow).on(
                        follow.sender.eq(member)
                                .and(follow.receiver.id.eq(memberId))
                                .and(follow.status.eq(ACTIVE))
                )
                .leftJoin(f).on(
                        f.sender.id.eq(loginMemberId)
                                .and(f.receiver.eq(member))
                                .and(f.status.eq(ACTIVE))
                )
                .leftJoin(block).on(checkBlockStatus(loginMemberId))
                .groupBy(member, follow, f)
                .having(block.id.count().eq(0L));
    }

    private BooleanExpression checkBlockStatus(final Long loginMemberId) {
        return checkMemberBlockLoginMember(loginMemberId).or(checkLoginMemberBlockMember(loginMemberId));
    }

    private BooleanExpression checkMemberBlockLoginMember(final Long loginMemberId) {
        return (block.blocker.eq(member))
                .and(block.blockedMember.id.eq(loginMemberId));
    }

    private BooleanExpression checkLoginMemberBlockMember(final Long loginMemberId) {
        return (block.blocker.id.eq(loginMemberId))
                .and(block.blockedMember.eq(member));
    }
}
