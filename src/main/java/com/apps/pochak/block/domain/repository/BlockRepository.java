package com.apps.pochak.block.domain.repository;

import com.apps.pochak.block.domain.Block;
import com.apps.pochak.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockRepository extends JpaRepository<Block, Long> {

    @EntityGraph(attributePaths = {"blockedMember"})
    Page<Block> findBlockByBlocker(
            final Member blocker,
            final Pageable pageable
    );

    void deleteByBlockerAndBlockedMember(Member blocker, Member blockedMember);
}
