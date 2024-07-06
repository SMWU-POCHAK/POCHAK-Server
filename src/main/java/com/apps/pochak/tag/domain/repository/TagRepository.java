package com.apps.pochak.tag.domain.repository;

import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.tag.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.INVALID_TAG_ID;
import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.NOT_MY_TAG;

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
}
