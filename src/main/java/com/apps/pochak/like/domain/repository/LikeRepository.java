package com.apps.pochak.like.domain.repository;

import com.apps.pochak.like.domain.LikeEntity;
import com.apps.pochak.like.dto.response.LikeElement;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<LikeEntity, Long> {
    @Query("select count(l) from LikeEntity l where l.likedPost = :post and l.status = 'ACTIVE'")
    int countByLikedPost(@Param("post") final Post post);

    @Query("select count(l) > 0 from LikeEntity l " +
            "where l.likeMember = :member " +
            "   and l.likedPost = :post " +
            "   and l.status = 'ACTIVE'")
    Boolean existsByLikeMemberAndLikedPost(
            @Param("member") final Member member,
            @Param("post") final Post post
    );

    Optional<LikeEntity> findByLikeMemberAndLikedPost(
            final Member member,
            final Post post
    );

    @Query(value = "select l from LikeEntity l where l.lastModifiedDate > :nowMinusOneHour ")
    List<LikeEntity> findModifiedLikeEntityWithinOneHour(@Param("nowMinusOneHour") final LocalDateTime nowMinusOneHour);

    @Query("select  " +
            "new com.apps.pochak.like.dto.response.LikeElement(" +
            "   m.handle, " +
            "   m.profileImage, " +
            "   m.name, " +
            "   (case when m.id <> :loginMemberId then (f.sender is not null) else nullif(m.id, :loginMemberId) end) " +
            ") " +
            "from LikeEntity l " +
            "join Member m on (l.likedPost = :post and l.likeMember = m and m.status = 'ACTIVE'" +
            "                       and m not in (select b.blockedMember from Block b where b.blocker.id = :loginMemberId) " +
            "                       and :loginMemberId not in (select b.blockedMember.id from Block b where b.blocker = m)) " +
            "left join Follow f on (f.sender.id = :loginMemberId and f.receiver = l.likeMember) and f.status = 'ACTIVE' " +
            "where l.status = 'ACTIVE' and l.likedPost = :post " +
            "order by f.lastModifiedDate desc ")
    List<LikeElement> findLikesAndIsFollow(
            @Param("loginMemberId") final Long loginMemberId,
            @Param("post") final Post post
    );

    @Modifying
    @Query("update LikeEntity like set like.status = 'DELETED' " +
            "where like.likeMember.id = :memberId or like.likedPost.owner.id = :memberId")
    void deleteLikeByMemberId(@Param("memberId") final Long memberId);

    @Modifying
    @Query("update LikeEntity l " +
            "set l.status = 'DELETED' " +
            "where (l.likeMember = :memberA and l.likedPost.id in (select p.id from Post p where p.owner = :memberB))" +
            "   or (l.likeMember = :memberB and l.likedPost.id in (select p.id from Post p where p.owner = :memberA))" +
            "   or (l.likeMember = :memberA and l.likedPost.id in (select t.post.id from Tag t where t.member = :memberB))" +
            "   or (l.likeMember = :memberB and l.likedPost.id in (select t.post.id from Tag t where t.member = :memberA))")
    void deleteLikesBetweenMembers(
            @Param("memberA") final Member memberA,
            @Param("memberB") final Member memberB
    );
}
