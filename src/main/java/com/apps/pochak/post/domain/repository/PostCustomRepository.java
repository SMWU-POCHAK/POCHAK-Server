package com.apps.pochak.post.domain.repository;

import com.apps.pochak.global.BaseEntityStatus;
import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.post.domain.PostStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.function.LongSupplier;

import static com.apps.pochak.block.domain.QBlock.block;
import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.BLOCKED_POST;
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

    public Optional<Post> findByIdWithoutBlockPost(
            final Long postId,
            final Long loginMemberId
    ) {
        return Optional.ofNullable(
                query.selectFrom(post)
                        .join(post.owner).fetchJoin()
                        .join(tag).on(tag.post.eq(post))
                        .leftJoin(block).on(
                                checkOwnerOrTaggedMemberBlockLoginMember(loginMemberId)
                                        .or(checkLoginMemberBlockOwnerOrTaggedMember(loginMemberId))
                        )
                        .groupBy(post)
                        .having(block.id.count().eq(0L))
                        .where(
                                post.id.eq(postId),
                                post.status.eq(BaseEntityStatus.ACTIVE)
                        )
                        .fetchOne()
        );
    }

    private BooleanExpression checkOwnerOrTaggedMemberBlockLoginMember(final Long loginMemberId) {
        return (block.blocker.eq(tag.member).or(block.blocker.eq(post.owner)))
                .and(block.blockedMember.id.eq(loginMemberId));
    }

    private BooleanExpression checkLoginMemberBlockOwnerOrTaggedMember(final Long loginMemberId) {
        return (block.blocker.id.eq(loginMemberId))
                .and(block.blockedMember.eq(tag.member).or(block.blockedMember.eq(post.owner)));
    }

    private JPAQuery<Post> findUploadPost(
            final Member owner,
            final Long loginMemberId
    ) {
        return query.selectFrom(post)
                .join(tag).on(tag.post.eq(post))
                .leftJoin(block).on(
                        (checkTaggedMemberBlockLoginMember(loginMemberId))
                                .or(checkLoginMemberBlockTaggedMember(loginMemberId))
                )
                .where(
                        post.owner.eq(owner),
                        post.status.eq(BaseEntityStatus.ACTIVE),
                        post.postStatus.eq(PostStatus.PUBLIC)
                )
                .groupBy(post)
                .having(block.count().eq(0L))
                .orderBy(post.allowedDate.desc());
    }

    public Page<Post> findUploadPostPage(
            final Member owner,
            final Long loginMemberId,
            final Pageable pageable
    ) {
        List<Post> contentPage = findUploadPost(owner, loginMemberId)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = query.select(post.count())
                .from(post)
                .where(post.in(findUploadPost(owner, loginMemberId)));

        return PageableExecutionUtils.getPage(contentPage, pageable, countQuery::fetchOne);
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