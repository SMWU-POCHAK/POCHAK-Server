package com.apps.pochak.login.dto.request;

import com.apps.pochak.global.annotation.ValidFile;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberInfoRequest {

    @NotNull(message = "사용자의 이름은 필수입니다.")
    private String name;

    private String email;

    @NotNull(message = "사용자의 별명은 필수입니다.")
    private String handle;

    private String message;

    @NotNull(message = "사용자의 소셜 아이디는 필수입니다.")
    private String socialId;

    @ValidFile(message = "프로필 이미지는 필수입니다.")
    private MultipartFile profileImage;

    @NotNull(message = "사용자의 소셜 로그인 플랫폼은 필수입니다.")
    private String socialType;

    private String socialRefreshToken;
}
