package com.apps.pochak.post.dto.request;

import com.apps.pochak.global.annotation.ValidFile;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.post.domain.Post;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostUploadRequest {

    @ValidFile(message = "게시물 이미지는 필수로 전달해야 합니다.")
    private MultipartFile postImage;

    private String caption;

    @Valid
    @Size(min = 1, message = "태그된 한 명 이상의 유저의 아이디를 전달해야 합니다.")
    @NotNull(message = "태그된 유저들의 아이디 리스트는 필수로 전달해야 합니다.")
    private List<String> taggedMemberHandleList;

    public Post toEntity(
            final String postImage,
            final Member owner
    ) {
        return Post.builder()
                .caption(this.caption)
                .postImage(postImage)
                .owner(owner)
                .build();
    }
}
