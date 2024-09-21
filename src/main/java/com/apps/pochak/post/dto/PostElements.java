package com.apps.pochak.post.dto;

import com.apps.pochak.global.util.PageInfo;
import com.apps.pochak.post.domain.Post;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostElements {
    private PageInfo pageInfo;
    private List<PostElement> postList;

    public static PostElements from(final Page<Post> postPage) {
        final PostElements postElements = new PostElements();
        postElements.setPageInfo(new PageInfo(postPage));

        final List<PostElement> postElementList = postPage.getContent().stream().map(
                PostElement::from
        ).collect(Collectors.toList());
        postElements.setPostList(postElementList);

        return postElements;
    }
}
