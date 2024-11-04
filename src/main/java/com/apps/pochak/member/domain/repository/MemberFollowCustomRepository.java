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
public class MemberFollowCustomRepository {
    private final JPAQueryFactory query;
    public static final QFollow f = new QFollow("f");

    public Page<MemberElement> findFollowersOfMemberAndIsFollow(
            final Long memberId,
            final Long loginMemberId,
            final Pageable pageable
    ) {
        JPQLQuery<Tuple> followerQuery = getFollowerQuery(memberId, loginMemberId);
        return getFollowerOrFollowingMemberElement(memberId, loginMemberId, followerQuery, pageable);
    }

    private JPQLQuery<Tuple> getFollowerQuery(
            final Long memberId,
            final Long loginMemberId
    ) {
        return getFollowerOrFollowingQuery(getFollowerCondition(memberId), loginMemberId);
    }

    private BooleanExpression getFollowerCondition(final Long memberId) {
        return follow.sender.eq(member)
                .and(follow.receiver.id.eq(memberId))
                .and(follow.status.eq(ACTIVE));
    }

    public Page<MemberElement> findFollowingsOfMemberAndIsFollow(
            final Long memberId,
            final Long loginMemberId,
            final Pageable pageable
    ) {
        JPQLQuery<Tuple> followingQuery = getFollowingQuery(memberId, loginMemberId);
        return getFollowerOrFollowingMemberElement(memberId, loginMemberId, followingQuery, pageable);
    }

    private JPQLQuery<Tuple> getFollowingQuery(
            final Long memberId,
            final Long loginMemberId
    ) {
        return getFollowerOrFollowingQuery(getFollowingCondition(memberId), loginMemberId);
    }

    private BooleanExpression getFollowingCondition(final Long memberId) {
        return follow.sender.id.eq(memberId)
                .and(follow.receiver.eq(member))
                .and(follow.status.eq(ACTIVE));
    }

    private JPQLQuery<Tuple> getFollowerOrFollowingQuery(
            final BooleanExpression followCondition,
            final Long loginMemberId
    ) {
        return query.select(member, follow, f)
                .from(member)
                .join(follow).on(followCondition)
                .leftJoin(f).on(
                        f.sender.id.eq(loginMemberId)
                                .and(f.receiver.eq(member))
                                .and(f.status.eq(ACTIVE))
                )
                .leftJoin(block).on(checkBlockStatus(loginMemberId))
                .groupBy(member, follow, f)
                .having(block.id.count().eq(0L));
    }

    private Page<MemberElement> getFollowerOrFollowingMemberElement(
            final Long memberId,
            final Long loginMemberId,
            final JPQLQuery<Tuple> followQuery,
            final Pageable pageable
    ) {
        List<MemberElement> memberElementList =
                followQuery
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
                                followQuery.select(member)
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
