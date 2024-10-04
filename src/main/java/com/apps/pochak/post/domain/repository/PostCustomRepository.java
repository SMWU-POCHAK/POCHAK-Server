package com.apps.pochak.post.domain.repository;

import com.apps.pochak.global.BaseEntityStatus;
import com.apps.pochak.global.api_payload.exception.GeneralException;
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

    public List<Post> findUploadPost(
            final Long ownerId,
            final Long loginMemberId
    ) {
        return query.selectFrom(post)
                .join(tag).on(tag.post.eq(post))
                .leftJoin(block).on(
                        (checkOwnerOrTaggedMemberBlockLoginMember(loginMemberId))
                                .or(checkLoginMemberBlockOwnerOrTaggedMember(loginMemberId))
                )
                .groupBy(post)
                .having(block.id.count().eq(0L))
                .where(
                        post.owner.id.eq(ownerId),
                        post.status.eq(BaseEntityStatus.ACTIVE),
                        post.postStatus.eq(PostStatus.PUBLIC)
                )
                .orderBy(post.allowedDate.desc())
                .fetch();
    }

    public Page<Post> findUploadPostPage(
            final Long ownerId,
            final Long loginMemberId,
            final Pageable pageable
    ) {
//        List<Post> contentPage = query.selectFrom(post)
//                .join(tag).on(tag.post.eq(post))
//                .leftJoin(block).on(
//                        (checkOwnerOrTaggedMemberBlockLoginMember(loginMemberId))
//                                .or(checkLoginMemberBlockOwnerOrTaggedMember(loginMemberId))
//                )
//                .groupBy(post)
//                .having(block.id.count().eq(0L))
//                .where(
//                        post.owner.id.eq(ownerId),
//                        post.status.eq(BaseEntityStatus.ACTIVE),
//                        post.postStatus.eq(PostStatus.PUBLIC)
//                )
//                .orderBy(post.allowedDate.desc())
//                .offset(pageable.getOffset())
//                .limit(pageable.getPageSize())
//                .fetch();
//
//        JPAQuery<Long> countQuery = query.select(post.count())
//                .from(post)
//                .join(tag).on(tag.post.eq(post))
//                .leftJoin(block).on(
//                        checkOwnerOrTaggedMemberBlockLoginMember(loginMemberId)
//                                .or(checkLoginMemberBlockOwnerOrTaggedMember(loginMemberId))
//                )
//                .groupBy(post)
//                .having(block.id.count().eq(0L))
//                .where(
//                        post.owner.id.eq(ownerId),
//                        post.status.eq(BaseEntityStatus.ACTIVE),
//                        post.postStatus.eq(PostStatus.PUBLIC)
//                );

        JPAQuery<Post> content = query.selectFrom(post)
                .join(tag).on(tag.post.eq(post))
                .leftJoin(block).on(
                        checkOwnerOrTaggedMemberBlockLoginMember(loginMemberId)
                                .or(checkLoginMemberBlockOwnerOrTaggedMember(loginMemberId))
                )
                .groupBy(post)
                .having(block.id.count().eq(0L))
                .where(
                        post.owner.id.eq(ownerId),
                        post.status.eq(BaseEntityStatus.ACTIVE),
                        post.postStatus.eq(PostStatus.PUBLIC)
                )
                .orderBy(post.allowedDate.desc());

//        JPAQuery<Long> countQuery = content.select(post.count());
//        LongSupplier count = countQuery::fetchOne;
        LongSupplier count = () -> content.fetch().size();

        List<Post> contentPage = content
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return PageableExecutionUtils.getPage(contentPage, pageable, count);
    }
}