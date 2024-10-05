package com.apps.pochak.memories.dto.response;

import com.apps.pochak.global.util.PageInfo;
import com.apps.pochak.post.dto.PostElement;
import com.apps.pochak.tag.domain.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemoriesPostResponse {
    private PageInfo pageInfo;
    private List<PostElement> postList;

    public static MemoriesPostResponse from(final Page<Tag> tags) {
        final MemoriesPostResponse memoriesPostResponse = new MemoriesPostResponse();
        memoriesPostResponse.setPageInfo(new PageInfo(tags));

        final List<PostElement> postElementList = tags.getContent().stream().map(
                PostElement::from
        ).collect(Collectors.toList());
        memoriesPostResponse.setPostList(postElementList);

        return memoriesPostResponse;
    }
}
