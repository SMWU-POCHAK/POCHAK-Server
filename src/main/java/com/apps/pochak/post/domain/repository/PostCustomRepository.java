package com.apps.pochak.post.domain.repository;

import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.post.domain.Post;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
                        .join(tag).on(tag.post.eq(post).and(post.id.eq(postId)))
                        .leftJoin(block).on(checkBlockStatus(loginMemberId))
                        .where(post.status.eq(ACTIVE).and(post.postStatus.eq(PUBLIC)))
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

        // TODO: 이게 최선인지.. 확인하기
        Long count = query
                .select(post.count())
                .from(post)
                .where(post.in(findPostOfFollowing(memberId)))
                .fetchOne();

        return new PageImpl<>(postList, pageable, count);
    }

    private JPQLQuery<Post> findPostOfFollowing(final Long memberId) {
        return query
                .selectFrom(post)
                .join(tag).on(
                        tag.post.eq(post)
                                .and(post.status.eq(ACTIVE))
                                .and(post.postStatus.eq(PUBLIC))
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
}
