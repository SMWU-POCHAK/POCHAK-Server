package com.apps.pochak.member.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileUpdateRequest {
    private String name;

    private String message;

    private MultipartFile profieImage;

}
