package com.apps.pochak.block.domain.repository;

import com.apps.pochak.block.domain.Block;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockRepository extends JpaRepository<Block, Long> {
}
