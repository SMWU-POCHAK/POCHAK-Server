package com.apps.pochak.block.dto.response;

import com.apps.pochak.block.domain.Block;
import com.apps.pochak.global.PageInfo;
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
public class BlockElements {
    private PageInfo pageInfo;
    private List<BlockElement> blockList;

    @Builder(builderMethodName = "from")
    public BlockElements(final Page<Block> blockPage) {
        this.pageInfo = new PageInfo(blockPage);
        this.blockList = blockPage.stream().map(BlockElement::new).collect(Collectors.toList());
    }
}
