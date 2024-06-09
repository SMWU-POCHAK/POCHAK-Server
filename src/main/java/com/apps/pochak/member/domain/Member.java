package com.apps.pochak.member.domain;

import com.apps.pochak.global.BaseEntity;
import com.apps.pochak.member.dto.request.ProfileUpdateRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

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

    private SocialType socialType;

    private String socialRefreshToken;

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Builder(builderMethodName = "signupMember", builderClassName = "signupMember")
    public Member(String name, String email, String handle, String message, String socialId, SocialType socialType, String profileImage, String socialRefreshToken) {
        this.handle = handle;
        this.name = name;
        this.message = message;
        this.email = email;
        this.profileImage = profileImage;
        this.socialId = socialId;
        this.socialType = socialType;
        this.socialRefreshToken = socialRefreshToken;
    }

    public void updateMember(ProfileUpdateRequest profileUpdateRequest, String profileImageUrl){
        this.name = getOrDefault(profileUpdateRequest.getName(), this.name);
        this.handle = getOrDefault(profileUpdateRequest.getHandle(), this.handle);
        this.message = getOrDefault(profileUpdateRequest.getMessage(), this.message);
        this.profileImage = getOrDefault(profileImageUrl, this.profileImage);
    }

    private <T> T getOrDefault(T property, T alternative){
        if (property != null)
            return property;
        return alternative;
    }
}
