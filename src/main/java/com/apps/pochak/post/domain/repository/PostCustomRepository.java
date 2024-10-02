package com.apps.pochak.post.domain.repository;

import com.apps.pochak.global.BaseEntityStatus;
import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.post.domain.Post;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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
                        .join(tag).on(tag.post.eq(post).and(post.id.eq(postId)))
                        .leftJoin(block).on(
                                checkOwnerOrTaggedMemberBlockLoginMember(loginMemberId)
                                        .or(checkLoginMemberBlockOwnerOrTaggedMember(loginMemberId))
                        )
                        .groupBy(post)
                        .having(block.id.count().eq(0L))
                        .where(post.status.eq(BaseEntityStatus.ACTIVE))
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
}
