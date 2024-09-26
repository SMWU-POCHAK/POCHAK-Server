package com.apps.pochak.tag.domain.repository;

import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.post.domain.PostStatus;
import com.apps.pochak.tag.domain.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.*;

public interface TagRepository extends JpaRepository<Tag, Long> {

    @Override
    @Query("select t from Tag t " +
            "join fetch t.member " +
            "join fetch t.post " +
            "where t.id = :id ")
    Optional<Tag> findById(@Param("id") Long id);

    default Tag findTagById(Long id) {
        return findById(id).orElseThrow(() -> new GeneralException(INVALID_TAG_ID));
    }

    default Tag findTagByIdAndMember(Long id, Member member) {
        final Tag tag = findTagById(id);
        if (!tag.getMember().getId().equals(member.getId())) {
            throw new GeneralException(NOT_MY_TAG);
        }
        return tag;
    }

    @Query("select t from Tag t " +
            "join fetch t.member " +
            "where t.post = :post ")
    List<Tag> findTagsByPost(@Param("post") final Post post);

    @Modifying
    @Query(value = """
            update tag t, alarm a set t.status = 'DELETED', a.status = 'DELETED'
            where (t.member_id = :memberId or t.post_id in :postIdList)
                and t.id = a.tag_approval_id
            """,
            nativeQuery = true)
    void deleteTagByMemberOrPostList(
            @Param("memberId") final Long memberId,
            @Param("postIdList") final List<Long> postIdList
    );

    @Modifying
    void deleteByPost(final Post post);

    @Query("""
    select t from Tag t
    join fetch t.member m
    join fetch t.post p
    where p.postStatus = 'PUBLIC'
    and m = :member
    and p.owner = :owner
    order by p.allowedDate asc
    """)
    Page<Tag> findTagByOwnerAndMember(@Param("owner")Member owner, @Param("member")Member member, Pageable pageable);

    @Query("""
    select t1
    from Tag t1
    join fetch t1.post p
    join Tag t2 on t1.post = t2.post
    where p.postStatus = 'PUBLIC'
    and t1.member = :loginMember
    and t2.member = :member
    """)
    Page<Tag> findTaggedWith(@Param("loginMember")Member loginMember, @Param("member")Member member, Pageable pageable);

    @Query("""
    select t from Tag t
    join fetch t.member m
    join fetch t.post p
    where p.postStatus = 'PUBLIC'
    and ((m = :member and p.owner = :loginMember)
    or (m = :loginMember and p.owner = :member))
    order by p.allowedDate desc
    """)
    Page<Tag> findLatestTagged(@Param("loginMember")Member loginMember, @Param("member")Member member, Pageable pageable);

    Long countByPost_PostStatusAndPost_OwnerAndMember(PostStatus postStatus, Member owner, Member member);

    @Query("""
    select count(t) from Tag t
    join t.member m
    join t.post p
    where p.postStatus = 'PUBLIC'
    and ((m = :member and p.owner = :loginMember)
    or (m = :loginMember and p.owner = :member))
    order by p.allowedDate desc
    """)
    Long countByMember(@Param("loginMember") Member loginMember, @Param("member")Member member);
}
