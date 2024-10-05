package com.apps.pochak.member.dto.response;

import com.apps.pochak.global.util.PageInfo;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.post.dto.PostElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private String handle;
    private String profileImage;
    private String name;
    private String message;
    private long totalPostNum;
    private long followerCount;
    private long followingCount;
    private Boolean isFollow;
    private PageInfo pageInfo;
    private List<PostElement> postList;

    @Builder(builderMethodName = "of")
    public ProfileResponse(final Member member,
                           final long followerCount,
                           final long followingCount,
                           final Boolean isFollow,
                           final Page<Post> postPage
    ) {
        this.handle = member.getHandle();
        this.profileImage = member.getProfileImage();
        this.name = member.getName();
        this.message = member.getMessage();
        this.totalPostNum = postPage.getTotalElements();
        this.followerCount = followerCount;
        this.followingCount = followingCount;
        this.isFollow = isFollow;
        this.pageInfo = new PageInfo(postPage);
        this.postList = postPage.getContent().stream().map(
                PostElement::from
        ).collect(Collectors.toList());
    }
}
