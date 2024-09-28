package com.apps.pochak.post.domain.repository;

import com.apps.pochak.global.TestQuerydslConfig;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.tag.domain.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static com.apps.pochak.member.fixture.MemberFixture.MEMBER1;
import static com.apps.pochak.member.fixture.MemberFixture.MEMBER2;
import static com.apps.pochak.post.fixture.PostFixture.PUBLIC_POST;
import static com.apps.pochak.tag.fixture.TagFixture.APPROVED_TAG;

@DataJpaTest
@Import(TestQuerydslConfig.class)
class PostCustomRepositoryTest {
    @Autowired
    PostCustomRepository postCustomRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TagRepository tagRepository;

    @BeforeEach
    void setUp() {
        memberRepository.save(MEMBER1);
        memberRepository.save(MEMBER2);

        PUBLIC_POST.makePublic();
        postRepository.save(PUBLIC_POST);
        tagRepository.save(APPROVED_TAG);
    }

    @DisplayName("차단된 게시물을 제외한 게시물이 조회된다.")
    @Test
    void findById() throws Exception{
        //given
        
        //when
        
        //then
    }

    @DisplayName("유효한 id가 없는 경우 조회되지 않는다.")
    @Test
    void findById_WhenIdIsInvalid() throws Exception{
        //given

        //when

        //then
    }

    @DisplayName("게시물을 업로드한 사람이 현재 로그인한 사람을 차단하였다면 조회되지 않는다.")
    @Test
    void findById_WhenOwnerBlockLoginMember() throws Exception{
        //given

        //when

        //then
    }

    @DisplayName("게시물에 태그된 사람 중 한명이라도 현재 로그인한 사람을 차단하였다면 조회되지 않는다.")
    @Test
    void findById_WhenTaggedMemberBlockLoginMember() throws Exception{
        //given

        //when

        //then
    }

    @DisplayName("현재 로그인한 사람이 게시물을 업로드한 사람을 차단하였다면 조회되지 않는다.")
    @Test
    void findById_WhenLoginMemberBlockOwner() throws Exception{
        //given

        //when

        //then
    }

    @DisplayName("현재 로그인한 사람이 게시물에 태그된 사람 중 한명이라도 차단하였다면 조회되지 않는다.")
    @Test
    void findPostByIdWithoutBlockPostWhenLoginMemberBlockTaggedMember() throws Exception{
        //given

        //when

        //then
    }
}