package com.apps.pochak.block.fixture;

import com.apps.pochak.block.domain.Block;

import static com.apps.pochak.member.fixture.MemberFixture.STATIC_MEMBER1;

public class BlockFixture {

    public static final Block STATIC_BLOCK = new Block(
            1L,
            STATIC_MEMBER1,
            STATIC_MEMBER1
    );
}
