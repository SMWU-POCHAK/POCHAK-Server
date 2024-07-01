package com.apps.pochak.member.domain;

import com.apps.pochak.global.BaseEntity;
import com.apps.pochak.member.dto.request.ProfileUpdateRequest;
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

    @Builder(builderMethodName = "signupMember", builderClassName = "signupMember")
    public Member(
            final String name,
            final String email,
            final String handle,
            final String message,
            final String socialId,
            final SocialType socialType,
            final String profileImage,
            final String refreshToken,
            final String socialRefreshToken
    ) {
        this.handle = handle;
        this.name = name;
        this.message = message;
        this.email = email;
        this.profileImage = profileImage;
        this.socialId = socialId;
        this.socialType = socialType;
        this.refreshToken = refreshToken;
        this.socialRefreshToken = socialRefreshToken;
    }

    public void updateMember(ProfileUpdateRequest profileUpdateRequest, String profileImageUrl){
        this.name = getOrDefault(profileUpdateRequest.getName(), this.name);
        this.message = getOrDefault(profileUpdateRequest.getMessage(), this.message);
        this.profileImage = getOrDefault(profileImageUrl, this.profileImage);
    }

    private <T> T getOrDefault(T property, T alternative){
        if (property != null)
            return property;
        return alternative;
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

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
