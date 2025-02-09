package com.apps.pochak.comment.domain.repository;

import com.apps.pochak.comment.domain.Comment;
import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.post.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.INVALID_COMMENT_ID;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = {"post"})
    Optional<Comment> findById(Long id);

    default Comment findCommentById(Long id) {
        return findById(id).orElseThrow(() -> new GeneralException(INVALID_COMMENT_ID));
    }

    @Query("select c from Comment c " +
            "join fetch c.member " +
            "where c.post = :post " +
            "   and c.member not in (select b.blockedMember from Block b where b.blocker = :loginMember) " +
            "   and :loginMember not in (select b.blockedMember from Block b where b.blocker = c.member) " +
            "order by c.createdDate desc limit 1")
    Optional<Comment> findFirstByPost(
            @Param("post") final Post post,
            @Param("loginMember") final Member loginMember
    );

    @Query("select c from Comment c " +
            "join fetch c.member " +
            "where c.post = :post " +
            "   and c.parentComment is null " +
            "   and c.member not in (select b.blockedMember from Block b where b.blocker = :loginMember)" +
            "   and :loginMember not in (select b.blockedMember from Block b where b.blocker = c.member) ")
    Page<Comment> findParentCommentByPost(
            @Param("post") final Post post,
            @Param("loginMember") final Member loginMember,
            Pageable pageable
    );

    @Query("""
            select c from Comment c
            join fetch c.member
            where c.id = :commentId
               and c.parentComment is null
               and c.member not in (select b.blockedMember from Block b where b.blocker = :loginMember)
               and :loginMember not in (select b.blockedMember from Block b where b.blocker = c.member)
            order by c.createdDate desc
            """)
    Optional<Comment> findParentCommentById(
            @Param("commentId") final Long commentId,
            @Param("loginMember") final Member loginMember
    );

    @Query("""
            select c from Comment c
            join fetch c.member
            where c.parentComment = :parentComment
               and c.member not in (select b.blockedMember from Block b where b.blocker = :loginMember)
               and :loginMember not in (select b.blockedMember from Block b where b.blocker = c.member)
            order by c.createdDate desc
            """)
    Page<Comment> findChildCommentByParentComment(@Param("parentComment") Comment parentComment,
                                                  @Param("loginMember") Member loginMember,
                                                  Pageable pageable);

    @Query(value = """
            select child_comments.*
            from (
                select c.*,
                       ROW_NUMBER() OVER (partition by c.parent_comment_id order by c.created_date desc) as row_num
                from comment c
                join member m on c.member_id = m.id
                where c.parent_comment_id in ?1
                  and c.member_id not in (select b.blocked_id from block b where b.blocker_id = ?2)
                  and ?2 not in (select b.blocked_id from block b where b.blocker_id = c.member_id)
            ) child_comments
            where child_comments.row_num = 1""", nativeQuery = true)
    List<Comment> findChildCommentByParentComments(List<Long> parentCommentIds, Long loginMemberId);

    @Modifying
    @Query("update Comment c set c.status = 'DELETED' " +
            "where c.post = :post ")
    void deleteByPost(@Param("post") final Post post);

    @Modifying
    @Query("update Comment comment " +
            "set comment.status = 'DELETED' " +
            "where comment.member.id = :memberId or comment.post.owner.id = :memberId ")
    void deleteCommentByMemberId(@Param("memberId") final Long memberId);

    @Modifying
    @Query("update Comment c " +
            "set c.status = 'DELETED' " +
            "where c.id = :id or c.parentComment.id = :id")
    void deleteCommentById(@Param("id") final Long id);

    @Modifying
    @Query(value = """
            update comment c, alarm a set c.status = 'DELETED', a.status = 'DELETED'
            where (c.member_id = :memberId or c.post_id in :postIdList)
                and c.id = a.comment_id
            """,
            nativeQuery = true)
    void deleteCommentByMemberOrPostList(
            @Param("memberId") final Long memberId,
            @Param("postIdList") final List<Long> postIdList
    );
}
