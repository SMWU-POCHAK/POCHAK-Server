package com.apps.pochak.block.fixture;

import com.apps.pochak.block.domain.Block;

import static com.apps.pochak.member.fixture.MemberFixture.BLOCKED_MEMBER;
import static com.apps.pochak.member.fixture.MemberFixture.MEMBER1;

public class BlockFixture {

    private static final Block BLOCK = new Block(
            1L,
            MEMBER1,
            BLOCKED_MEMBER
    );
}
