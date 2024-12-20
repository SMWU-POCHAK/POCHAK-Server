package com.apps.pochak.post.domain.repository;

import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.post.domain.Post;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.apps.pochak.block.domain.QBlock.block;
import static com.apps.pochak.follow.domain.QFollow.follow;
import static com.apps.pochak.global.BaseEntityStatus.ACTIVE;
import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.BLOCKED_POST;
import static com.apps.pochak.post.domain.PostStatus.PUBLIC;
import static com.apps.pochak.post.domain.QPost.post;
import static com.apps.pochak.tag.domain.QTag.tag;

@Repository
@RequiredArgsConstructor
public class PostCustomRepository {
    private final JPAQueryFactory query;

    public Post findPostByIdWithoutBlockPost(
            final Long postId,
            final Long loginMemberId
    ) {
        return findByIdWithoutBlockPost(postId, loginMemberId).orElseThrow(() -> new GeneralException(BLOCKED_POST));
    }

    private Optional<Post> findByIdWithoutBlockPost(
            final Long postId,
            final Long loginMemberId
    ) {
        return Optional.ofNullable(
                query.selectFrom(post)
                        .join(post.owner).fetchJoin()
                        .join(tag).on(
                                tag.post.eq(post)
                                        .and(post.id.eq(postId))
                                        .and(checkPublicPost())
                        )
                        .leftJoin(block).on(checkBlockStatus(loginMemberId))
                        .groupBy(post)
                        .having(block.id.count().eq(0L))
                        .fetchOne()
        );
    }

    public Page<Post> findPostOfFollowing(
            final Long memberId,
            final Pageable pageable
    ) {
        List<Post> postList = findPostOfFollowing(memberId)
                .orderBy(post.allowedDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = query
                .select(post.count())
                .from(post)
                .where(post.in(findPostOfFollowing(memberId)));

        return PageableExecutionUtils.getPage(postList, pageable, countQuery::fetchOne);
    }

    private JPQLQuery<Post> findPostOfFollowing(final Long memberId) {
        return query
                .selectFrom(post)
                .join(tag).on(
                        tag.post.eq(post)
                                .and(checkPublicPost())
                )
                .leftJoin(follow).on(checkFollowOwnerOrTaggedMember(memberId))
                .leftJoin(block).on(checkBlockStatus(memberId))
                .where(
                        follow.id.isNotNull()
                                .or(block.id.isNotNull())
                                .or(post.owner.id.eq(memberId))
                                .or(tag.member.id.eq(memberId))
                )
                .groupBy(post)
                .having(block.id.count().eq(0L));
    }

    public Page<Post> findUploadPost(
            final Member owner,
            final Long loginMemberId,
            final Pageable pageable
    ) {
        List<Post> postList = findUploadPost(owner, loginMemberId)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = query.select(post.count())
                .from(post)
                .where(post.in(findUploadPost(owner, loginMemberId)));

        return PageableExecutionUtils.getPage(postList, pageable, countQuery::fetchOne);
    }

    private JPAQuery<Post> findUploadPost(
            final Member owner,
            final Long loginMemberId
    ) {
        return query.selectFrom(post)
                .join(tag).on(
                        tag.post.eq(post)
                                .and(checkPublicPost())
                )
                .leftJoin(block).on(
                        (checkTaggedMemberBlockLoginMember(loginMemberId))
                                .or(checkLoginMemberBlockTaggedMember(loginMemberId))
                )
                .where(
                        post.owner.eq(owner)
                )
                .groupBy(post)
                .having(block.count().eq(0L))
                .orderBy(post.allowedDate.desc());
    }

    private BooleanExpression checkPublicPost() {
        return post.status.eq(ACTIVE)
                .and(post.postStatus.eq(PUBLIC));
    }

    private BooleanExpression checkFollowOwnerOrTaggedMember(final Long memberId) {
        return checkFollowOwner(memberId).or(checkFollowTaggedMember(memberId));
    }

    private BooleanExpression checkFollowOwner(final Long loginMemberId) {
        return follow.receiver.eq(post.owner)
                .and(follow.sender.id.eq(loginMemberId))
                .and(follow.status.eq(ACTIVE));
    }

    private BooleanExpression checkFollowTaggedMember(final Long loginMemberId) {
        return follow.receiver.eq(tag.member)
                .and(follow.sender.id.eq(loginMemberId))
                .and(follow.status.eq(ACTIVE));
    }

    private BooleanExpression checkBlockStatus(final Long loginMemberId) {
        return checkOwnerOrTaggedMemberBlockLoginMember(loginMemberId)
                .or(checkLoginMemberBlockOwnerOrTaggedMember(loginMemberId));
    }

    private BooleanExpression checkOwnerOrTaggedMemberBlockLoginMember(final Long loginMemberId) {
        return (block.blocker.eq(tag.member).or(block.blocker.eq(post.owner)))
                .and(block.blockedMember.id.eq(loginMemberId));
    }

    private BooleanExpression checkLoginMemberBlockOwnerOrTaggedMember(final Long loginMemberId) {
        return (block.blocker.id.eq(loginMemberId))
                .and(block.blockedMember.eq(tag.member).or(block.blockedMember.eq(post.owner)));
    }

    private BooleanExpression checkTaggedMemberBlockLoginMember(final Long loginMemberId) {
        return (block.blocker.eq(tag.member))
                .and(block.blockedMember.id.eq(loginMemberId));
    }

    private BooleanExpression checkLoginMemberBlockTaggedMember(final Long loginMemberId) {
        return (block.blocker.id.eq(loginMemberId))
                .and(block.blockedMember.eq(tag.member));
    }
}