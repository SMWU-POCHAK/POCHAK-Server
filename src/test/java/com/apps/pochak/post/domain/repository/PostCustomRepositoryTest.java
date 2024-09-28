package com.apps.pochak.post.domain.repository;

import com.apps.pochak.global.TestQuerydslConfig;
import com.apps.pochak.member.domain.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(TestQuerydslConfig.class)
class PostCustomRepositoryTest {
    @Autowired
    PostCustomRepository postCustomRepository;

    @Autowired
    MemberRepository memberRepository;

    
}