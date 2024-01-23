package com.apps.pochak.post.domain.repository;

import com.apps.pochak.member.domain.Member;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.post.domain.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {
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

    @Modifying
    @Query("update Post post set post.status = 'DELETED' where post.owner.id = :memberId")
    void deletePostByMemberId(@Param("memberId") final Long memberId);
}
