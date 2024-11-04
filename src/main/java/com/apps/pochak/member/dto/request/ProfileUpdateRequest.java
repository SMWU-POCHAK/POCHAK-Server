package com.apps.pochak.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileUpdateRequest {
    @NotNull(message = "사용자의 이름은 필수입니다.")
    @NotBlank(message = "이름은 공백이 될 수 없습니다.")
    @Size(max = 15, message = "이름은 최대 15자까지 가능합니다.")
    private String name;

    @Pattern(regexp = "^(.+\n?){0,3}$", message = "소개는 최대 3줄까지 가능합니다.")
    @Size(max = 50, message = "소개는 최대 50자까지 가능합니다.")
    private String message;

    private MultipartFile profileImage;
}
