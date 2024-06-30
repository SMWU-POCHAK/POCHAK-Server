package com.apps.pochak.post.domain;

import com.apps.pochak.global.BaseEntity;
import com.apps.pochak.member.domain.Member;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

import static com.apps.pochak.post.domain.PostStatus.PRIVATE;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@DynamicInsert
@NoArgsConstructor(access = PROTECTED)
@SQLDelete(sql = "UPDATE post SET status = 'DELETED' WHERE id = ?")
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Enumerated(STRING)
    @Setter
    @Column(columnDefinition = "VARCHAR(255) DEFAULT 'PRIVATE'")
    private PostStatus postStatus;

    private LocalDateTime allowedDate;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "owner_id")
    private Member owner;

    private String postImage;

    private String caption;

    @Builder
    public Post(
            final Member owner,
            final String postImage,
            final String caption
    ) {
        this.owner = owner;
        this.postImage = postImage;
        this.caption = caption;
    }

    public boolean isPrivate() {
        return getPostStatus().equals(PRIVATE);
    }

    public boolean isOwner(final Member member) {
        return this.owner.getId().equals(member.getId());
    }

    public void makePublic() {
        this.allowedDate = LocalDateTime.now();
        this.postStatus = PostStatus.PUBLIC;
    }
}
