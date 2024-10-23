package com.apps.pochak.login.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberInfoRequest {

    @NotNull(message = "사용자의 이름은 필수입니다.")
    @NotBlank(message = "이름은 공백이 될 수 없습니다.")
    @Size(max = 15, message = "이름은 최대 15자까지 가능합니다.")
    private String name;

    @Email
    private String email;

    @NotNull(message = "사용자의 핸들은 필수입니다.")
    @Pattern(regexp = "^[A-Za-z0-9_.]+$", message = "핸들은 대문자, 소문자, 숫자, _(언더바), .(마침표)만 가능합니다.")
    @Size(max = 15, message = "핸들은 최대 15자까지 가능합니다.")
    private String handle;

    @Pattern(regexp = "^(.+\n?){0,3}$", message = "소개는 최대 3줄까지 가능합니다.")
    @Size(max = 50, message = "소개는 최대 50자까지 가능합니다.")
    private String message;

    @NotNull(message = "사용자의 소셜 아이디는 필수입니다.")
    private String socialId;

    private MultipartFile profileImage;

    @NotNull(message = "사용자의 소셜 로그인 플랫폼은 필수입니다.")
    private String socialType;

    private String socialRefreshToken;
}
