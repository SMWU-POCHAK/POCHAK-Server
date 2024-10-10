package com.apps.pochak.like.domain;

import com.apps.pochak.global.BaseEntity;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.post.domain.Post;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
@SQLDelete(sql = "UPDATE like_entity SET status = 'DELETED' WHERE id = ?")
public class LikeEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "like_member_id")
    private Member likeMember;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "liked_post_id")
    private Post likedPost;

    @Builder
    public LikeEntity(Member likeMember, Post likedPost) {
        this.likeMember = likeMember;
        this.likedPost = likedPost;
    }
}
