package com.apps.pochak.post.domain.repository;

import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.tag.domain.Tag;
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
            "   and :loginMember not in (select b.blockedMember from Block b where b.blocker = p.owner) " +
            "   and not exists (select t.member from Tag t where t.post = p intersect select b.blockedMember from Block b where b.blocker = :loginMember) " +
            "   and :loginMember not in (select b.blockedMember from Block b where b.blocker in (select t.member from Tag t where t.post = p)) "
    )
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
            "   and p.owner not in (select b.blockedMember from Block b where b.blocker = :loginMember) " +
            "   and :loginMember not in (select b.blockedMember from Block b where b.blocker = p.owner) " +
            "   and not exists (select t.member from Tag t where t.post = p intersect select b.blockedMember from Block b where b.blocker = :loginMember) " +
            "   and :loginMember not in (select b.blockedMember from Block b where b.blocker in (select t.member from Tag t where t.post = p)) " +
            "order by t.lastModifiedDate desc ")
    Page<Post> findTaggedPost(
            @Param("member") final Member member,
            @Param("loginMember") final Member loginMember,
            final Pageable pageable
    );

    @Query("select p from Post p " +
            "where p.owner = :owner and p.status = 'ACTIVE' and p.postStatus = 'PUBLIC' " +
            "   and p.owner not in (select b.blockedMember from Block b where b.blocker = :loginMember) " +
            "   and :loginMember not in (select b.blockedMember from Block b where b.blocker = p.owner) " +
            "   and not exists (select t.member from Tag t where t.post = p intersect select b.blockedMember from Block b where b.blocker = :loginMember) " +
            "   and :loginMember not in (select b.blockedMember from Block b where b.blocker in (select t.member from Tag t where t.post = p)) " +
            "order by p.createdDate desc ")
    Page<Post> findUploadPost(
            @Param("owner") final Member owner,
            @Param("loginMember") final Member loginMember,
            final Pageable pageable
    );

    @Query("select distinct p from Post p " +
            "join Tag t on p = t.post and p.postStatus = 'PUBLIC' and t.status = 'ACTIVE' and " +
            "   ( " +
            "       t.member in ( " +    // follow members tagged in post
            "           select f.receiver from Follow f where f.sender = :loginMember and f.status = 'ACTIVE' " +
            "       ) " +
            "       or p.owner in (" +   // follow owner of post
            "           select f.receiver from Follow f where f.sender = :loginMember and f.status = 'ACTIVE' " +
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

    @Query("""
            select p from Post p
            join Tag t on t.post = p and t = :tag
            join fetch p.owner
            where p.status = 'ACTIVE'
            """)
    Optional<Post> findPostByTag(@Param("tag") final Tag tag);

    @Query("select p from Post p " +
            "where p.postStatus = 'PUBLIC' and p.status = 'ACTIVE' and p.lastModifiedDate > :nowMinusOneHour ")
    List<Post> findModifiedPostWithinOneHour(@Param("nowMinusOneHour") final LocalDateTime nowMinusOneHour);

    @Query(value = "select * from post as p " +
            "where p.id in :postIdList and p.status = 'ACTIVE' " +
            "   and p.owner_id not in (select b.blocked_id from block b where b.blocker_id = :loginMemberId) " +
            "   and :loginMemberId not in (select b.blocked_id from block b where b.blocker_id = p.owner_id) " +
            "   and not exists (select t.member_id from tag t where t.post_id = p.id intersect select b.blocked_id from block b where b.blocker_id = :loginMemberId) " +
            "   and :loginMemberId not in (select b.blocked_id from block b where b.blocker_id in (select t.member_id from tag t where t.post_id = p.id)) " +
            "order by find_in_set(id, :postIdStrList) ",
            nativeQuery = true)
    Page<Post> findPostsIn(
            @Param("postIdList") final List<Long> postIdList,
            @Param("postIdStrList") final String postIdStrList,
            @Param("loginMemberId") final Long loginMemberId,
            final Pageable pageable
    );

    default Page<Post> findPostsInIdList(
            @Param("postIdList") final List<Long> postIdList,
            final Long loginMemberId,
            final Pageable pageable
    ) {
        final String postIdStrList = convertLongListToString(postIdList);
        return findPostsIn(
                postIdList,
                postIdStrList,
                loginMemberId,
                pageable
        );
    }

    @Query("""
            select p.id from Post p
            join Tag t on t.post = p
            where (p.owner = :member or t.member = :member) and p.status != 'DELETED'
            """)
    List<Long> findPostIdListByOwnerOrTaggedMember(@Param("member") final Member member);

    @Query("select p from Post p " +
            "left join LikeEntity l on l.likedPost = p " +
            "where p.postStatus = 'PUBLIC' and p.status = 'ACTIVE' " +
            "group by p.id " +
            "order by count(l) desc, p.allowedDate desc ")
    Page<Post> findPopularPost(final Pageable pageable);

    @Modifying
    @Query("update Post p SET p.status = 'INACTIVE' " +
            "where (p.owner = :memberA and p in (select t.post from Tag t where t.post = p and t.member = :memberB)) " +
            "   or (p.owner = :memberB and p in (select t.post from Tag t where t.post = p and t.member = :memberA)) " +
            "   or (:memberA in (select t.member from Tag t where t.post = p) " +
            "       and :memberB in (select t.member from Tag t where t.post = p))")
    void setPostInactiveBetweenMembers(
            @Param("memberA") final Member memberA,
            @Param("memberB") final Member memberB
    );

    @Modifying
    @Query("update Post p SET p.status = 'ACTIVE' " +
            "where p.status = 'INACTIVE' " +
            "   and p.owner not in (select b.blockedMember from Block b where b.blocker in (select t.member from Tag t where t.post = p)) " +
            "   and not exists (select t.member from Tag t where t.post = p " +
            "                   intersect " +
            "                   select b.blockedMember from Block b where b.blocker = p.owner or b.blocker in (select t.member from Tag t where t.post = p))")
    void reactivatePostBetweenMembers(
            @Param("memberA") final Member memberA,
            @Param("memberB") final Member memberB
    );

    @Modifying
    @Query("update Post p set p.status = 'DELETED' where p.id in :postIdList")
    void deleteAllPost(@Param("postIdList") final List<Long> postIdList);
}
