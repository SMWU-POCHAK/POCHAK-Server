package com.apps.pochak.post.dto.request;

import com.apps.pochak.global.annotation.ValidDuplicateList;
import com.apps.pochak.global.annotation.ValidFile;
import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.post.domain.Post;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.TAG_INVALID_MEMBER;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostUploadRequest {

    @ValidFile(message = "게시물 이미지는 필수로 전달해야 합니다.")
    private MultipartFile postImage;

    private String caption;

    private String pinnedHandle;

    @Size(min = 1, max = 5, message = "유저는 1명 이상, 5명 이하로 태그 가능합니다.")
    @ValidDuplicateList(message = "유저의 핸들은 중복 전달할 수 없습니다.")
    private List<String> taggedMemberHandleList;

    public PostUploadRequest(
            final MultipartFile postImage,
            final String caption,
            final List<String> taggedMemberHandleList
    ) {
        this.postImage = postImage;
        this.caption = caption;
        this.taggedMemberHandleList = taggedMemberHandleList;
    }

    public Post toEntity(
            final Member owner
    ) {
        return Post.builder()
                .caption(this.caption)
                .owner(owner)
                .build();
    }

    public Post toEntity(
            final String postImage,
            final Member owner,
            final Member pinnedMember
    ) {
        return Post.builder()
                .caption(this.caption)
                .postImage(postImage)
                .owner(owner)
                .pinnedMember(pinnedMember)
                .build();
    }

    @AssertTrue(message = "한 명 이상의 유저를 태그해야 합니다.")
    public boolean validateTaggedMember() {
        return pinnedHandle != null || (taggedMemberHandleList != null && !taggedMemberHandleList.isEmpty());
    }

    public List<String> getAllTaggedMember() {
        List<String> temp = new ArrayList<>(taggedMemberHandleList);
        if (pinnedHandle != null) {
            temp.add(pinnedHandle);
        }
        return temp;
    }

    public void validateMemberNotTagged(
            final Member member
    ) {
        if (taggedMemberHandleList.contains(member.getHandle())) {
            throw new GeneralException(TAG_INVALID_MEMBER);
        }
    }
}
