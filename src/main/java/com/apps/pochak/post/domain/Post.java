package com.apps.pochak.post.domain;

import com.apps.pochak.global.BaseEntity;
import com.apps.pochak.member.domain.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDateTime;

import static com.apps.pochak.post.domain.PostStatus.PRIVATE;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
@SQLDelete(sql = "UPDATE post SET status = 'DELETED' WHERE id = ?")
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Enumerated(STRING)
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
        this.postStatus = PRIVATE;
    }

    public boolean isPrivate() {
        return getPostStatus().equals(PRIVATE);
    }

    public boolean isOwner(final Member member) {
        return this.owner.equals(member);
    }

    public void makePublic() {
        this.allowedDate = LocalDateTime.now();
        this.postStatus = PostStatus.PUBLIC;
    }
}
