package com.apps.pochak.post.domain.repository;

import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.post.domain.Post;
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
import static com.apps.pochak.global.converter.LongListToStringConverter.convertLongListToString;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("select p from Post p " +
            "join fetch p.owner " +
            "where p.id = :postId and p.status = 'ACTIVE' " +
            "   and p.owner not in (select b.blockedMember from Block b where b.blocker = :loginMember) " +
            "   and (select t.member from Tag t where t.post = p) not in (select b.blockedMember from Block b where b.blocker = :loginMember) ")
    Optional<Post> findById(
            @Param("postId") final Long postId,
            @Param("loginMember") final Member loginMember
    );

    default Post findPostById(
            final Long postId,
            final Member loginMember
    ) {
        return findById(postId, loginMember).orElseThrow(() -> new GeneralException(INVALID_POST_ID));
    }

    default Post findPublicPostById(
            final Long postId,
            final Member loginMember
    ) {
        final Post post = findById(postId, loginMember).orElseThrow(() -> new GeneralException(INVALID_POST_ID));
        if (post.isPrivate()) {
            throw new GeneralException(PRIVATE_POST);
        }
        return post;
    }

    @Query("select p from Post p " +
            "join Tag t on ( t.post = p and t.member = :member and p.postStatus = 'PUBLIC' ) " +
            "where p.status = 'ACTIVE'" +
            "order by t.lastModifiedDate desc ")
    Page<Post> findTaggedPost(@Param("member") final Member member,
                              final Pageable pageable);

    @Query("select p from Post p " +
            "where (p.owner = :owner and p.owner = :loginMember and p.status = 'ACTIVE') " +
            "   or (p.owner = :owner and p.postStatus = 'PUBLIC' and p.status = 'ACTIVE')" +
            "order by p.createdDate desc ")
    Page<Post> findUploadPost(
            final Member owner,
            final Member loginMember,
            final Pageable pageable
    );


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
            "where p.status = 'ACTIVE'" +
            "order by p.allowedDate desc "
    )
    Page<Post> findTaggedPostsOfFollowing(
            @Param("loginMember") final Member loginMember,
            final Pageable pageable
    );

    @Modifying
    @Query("update Post post set post.status = 'DELETED' where post.owner.id = :memberId")
    void deletePostByMemberId(@Param("memberId") final Long memberId);

    @Query("select p from Post p " +
            "where p.postStatus = 'PUBLIC' and p.status = 'ACTIVE' and p.lastModifiedDate > :nowMinusOneHour ")
    List<Post> findModifiedPostWithinOneHour(@Param("nowMinusOneHour") final LocalDateTime nowMinusOneHour);

    @Query(value = "select * from post as p " +
            "where p.id in :postIdList and p.status = 'ACTIVE' " +
            "order by find_in_set(id, :postIdStrList) ",
            nativeQuery = true)
    Page<Post> findPostsIn(
            @Param("postIdList") final List<Long> postIdList,
            @Param("postIdStrList") final String postIdStrList,
            final Pageable pageable
    );

    default Page<Post> findPostsInIdList(
            @Param("postIdList") final List<Long> postIdList,
            final Pageable pageable
    ) {
        final String postIdStrList = convertLongListToString(postIdList);
        return findPostsIn(
                postIdList,
                postIdStrList,
                pageable
        );
    }

    @Query("update Post p SET p.status = 'INACTIVE' " +
            "where (p.owner = :memberA and p.id in (select t.post.id from Tag t where t.post = p and t.member = :memberB)) " +
            "   or (p.owner = :memberB and p.id in (select t.post.id from Tag t where t.post = p and t.member = :memberA)) " +
            "   or (:memberA in (select t.member from Tag t where t.post = p) " +
            "       and :memberB in (select t.member from Tag t where t.post = p))")
    void setPostInactiveBetweenMembers(
            @Param("memberA") final Member memberA,
            @Param("memberB") final Member memberB
    );
}
