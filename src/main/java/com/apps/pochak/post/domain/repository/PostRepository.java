package com.apps.pochak.post.domain.repository;

import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.global.converter.LongListToStringConverter;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.post.domain.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.INVALID_POST_ID;
import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.PRIVATE_POST;

public interface PostRepository extends JpaRepository<Post, Long> {


    @Override
    @Query("select p from Post p " +
            "join fetch p.owner " +
            "where p.id = :postId ")
    Optional<Post> findById(@Param("postId") final Long postId);

    default Post findPostById(final Long postId) {
        return findById(postId).orElseThrow(() -> new GeneralException(INVALID_POST_ID));
    }

    default Post findPublicPostById(final Long postId) {
        final Post post = findById(postId).orElseThrow(() -> new GeneralException(INVALID_POST_ID));
        if (post.isPrivate()) {
            throw new GeneralException(PRIVATE_POST);
        }
        return post;
    }

    @Query(value =
            "select p from Post p " +
                    "join Tag t " +
                    "on ( t.post = p and t.member = :member and p.postStatus = 'PUBLIC' ) " +
                    "order by t.lastModifiedDate desc ")
    Page<Post> findTaggedPost(@Param("member") final Member member,
                              final Pageable pageable);

    Page<Post> findPostByOwnerOrderByCreatedDateDesc(final Member owner, final Pageable pageable);

    Page<Post> findPostByOwnerAndPostStatusOrderByCreatedDateDesc(final Member owner,
                                                                  final PostStatus postStatus,
                                                                  final Pageable pageable);

    @Query("select distinct p from Post p " +
            "join Tag t on p = t.post and p.postStatus = 'PUBLIC' and t.status = 'ACTIVE' and " +
            "   ( " +
            "       t.member.id in ( " +    // follow members tagged in post
            "           select f.receiver.id from Follow f where f.sender = :loginMember and f.status = 'ACTIVE' " +
            "       ) " +
            "       or p.owner.id in (" +   // follow owner of post
            "           select f.receiver.id from Follow f where f.sender = :loginMember and f.status = 'ACTIVE' " +
            "       ) " +
            "       or t.member = :loginMember " +  // tagged in
            "       or p.owner = :loginMember " +  // owner
            "   ) " +
            "order by p.allowedDate desc "
    )
    Page<Post> findTaggedPostsOfFollowing(
            @Param("loginMember") final Member loginMember,
            final Pageable pageable
    );

    @Modifying
    @Query("update Post post set post.status = 'DELETED' where post.owner.id = :memberId")
    void deletePostByMemberId(@Param("memberId") final Long memberId);

    @Query("select p from Post p where p.postStatus = 'PUBLIC' and p.lastModifiedDate > :nowMinusOneHour ")
    List<Post> findModifiedPostWithinOneHour(@Param("nowMinusOneHour") final LocalDateTime nowMinusOneHour);

    @Query(
            value =
                    "select * from post where id in :postIdList order by find_in_set(id, :postIdStrList) ", nativeQuery = true
    )
    Page<Post> findPostsIn(
            @Param("postIdList") final List<Long> postIdList,
            @Param("postIdStrList") final String postIdStrList,
            final Pageable pageable
    );

    default Page<Post> findPostsInIdList(
            @Param("postIdList") final List<Long> postIdList,
            final Pageable pageable
    ) {
        final String postIdStrList = LongListToStringConverter.convertLongListToString(postIdList);
        return findPostsIn(
                postIdList,
                postIdStrList,
                pageable
        );
    }

}
