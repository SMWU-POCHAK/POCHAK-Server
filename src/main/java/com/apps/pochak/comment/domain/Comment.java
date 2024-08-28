package com.apps.pochak.comment.domain;

import com.apps.pochak.global.BaseEntity;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.post.domain.Post;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@DynamicInsert
@NoArgsConstructor(access = PROTECTED)
@SQLDelete(sql = "UPDATE comment SET status = 'DELETED' WHERE id = ?")
@SQLRestriction("status = 'ACTIVE'")
public class Comment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String content;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @OneToMany(mappedBy = "parentComment", cascade = ALL)
    private List<Comment> childCommentList = new ArrayList<>();

    public Comment(
            final String content,
            final Member member,
            final Post post
    ) {
        this.content = content;
        this.member = member;
        this.post = post;
    }

    public Comment(
            final Long id,
            final String content,
            final Member member,
            final Post post
    ) {
        this(content, member, post);
        this.id = id;
    }

    public Comment(
            final String content,
            final Member member,
            final Post post,
            final Comment parentComment
    ) {
        this(content, member, post);
        this.parentComment = parentComment;
        parentComment.getChildCommentList().add(this);
    }

    public Comment(
            final Long id,
            final String content,
            final Member member,
            final Post post,
            final Comment parentComment
    ) {
        this(content, member, post, parentComment);
        this.id = id;
    }

    public Boolean isChildComment() {
        return this.parentComment != null;
    }

    public boolean isOwner(final Member member) {
        return this.member.getId().equals(member.getId());
    }
}
