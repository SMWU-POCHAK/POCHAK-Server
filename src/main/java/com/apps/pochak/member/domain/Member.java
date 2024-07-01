package com.apps.pochak.member.domain;

import com.apps.pochak.global.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.Objects;

import static jakarta.persistence.EnumType.*;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@DynamicInsert
@NoArgsConstructor(access = PROTECTED)
@SQLDelete(sql = "UPDATE member SET status = 'DELETED' WHERE id = ?")
@SQLRestriction("status = 'ACTIVE'")
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(unique = true)
    private String handle;

    private String name;

    private String message;

    private String email;

    private String profileImage;

    private String refreshToken;

    private String socialId;

    @Enumerated(STRING)
    private SocialType socialType;

    private String socialRefreshToken;

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Builder(builderMethodName = "signupMember", builderClassName = "signupMember")
    public Member(
            final String name,
            final String email,
            final String handle,
            final String message,
            final String socialId,
            final SocialType socialType,
            final String profileImage,
            final String socialRefreshToken
    ) {
        this.handle = handle;
        this.name = name;
        this.message = message;
        this.email = email;
        this.profileImage = profileImage;
        this.socialId = socialId;
        this.socialType = socialType;
        this.socialRefreshToken = socialRefreshToken;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }

        Member member = (Member)o;
        return Objects.equals(id, member.id);
    }
}
