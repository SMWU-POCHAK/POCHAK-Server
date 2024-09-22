package com.apps.pochak.tag.domain.repository;

import com.apps.pochak.global.BaseEntityStatus;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.SocialType;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.post.domain.repository.PostRepository;
import com.apps.pochak.tag.domain.Tag;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(locations = "classpath:application.properties")
class TagRepositoryTest {
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        Member owner = Member.signupMember()
                .name("kimyeji")
                .email("kimyeji@gmail.com")
                .handle("kky.yjj2")
                .message("김예지입니다 탕.")
                .socialId("kimyeji")
                .profileImage("")
                .refreshToken("")
                .socialType(SocialType.GOOGLE)
                .socialRefreshToken("")
                .build();
        memberRepository.save(owner);

        Member member = Member.signupMember()
                .name("kangminji")
                .email("kangminji@gmail.com")
                .handle("mmn_mj")
                .message("강민지 ㅎ2")
                .socialId("kangminji")
                .profileImage("")
                .refreshToken("")
                .socialType(SocialType.GOOGLE)
                .socialRefreshToken("")
                .build();
        memberRepository.save(member);

        Member friend = Member.signupMember()
                .name("leehyunjin")
                .email("leehyunjin@gmail.com")
                .handle("hyunjiny2")
                .message("이현진")
                .socialId("leehyunjin")
                .profileImage("")
                .refreshToken("")
                .socialType(SocialType.GOOGLE)
                .socialRefreshToken("")
                .build();
        memberRepository.save(friend);

        Post post = Post.builder()
                .caption("푸핫 바보같아~!")
                .postImage("")
                .owner(owner)
                .build();
        postRepository.save(post);
        post.makePublic();
        post.setStatus(BaseEntityStatus.ACTIVE);

        Tag tag = Tag.builder()
                .member(member)
                .post(post)
                .build();
        tagRepository.save(tag);
        tag.setStatus(BaseEntityStatus.ACTIVE);

        Post post2 = Post.builder()
                .caption("너네 둘!")
                .postImage("")
                .owner(friend)
                .build();
        postRepository.save(post2);
        post2.makePublic();
        post2.setStatus(BaseEntityStatus.ACTIVE);

        Tag tags1 = Tag.builder()
                .member(owner)
                .post(post2)
                .build();
        tagRepository.save(tags1);
        tags1.setStatus(BaseEntityStatus.ACTIVE);
        Tag tags2 = Tag.builder()
                .member(member)
                .post(post2)
                .build();
        tagRepository.save(tags2);
        tags2.setStatus(BaseEntityStatus.ACTIVE);
    }

    @Test
    @DisplayName("[추억 페이지] 친구를 태그한 첫 게시물")
    void findFirstTaggedPost() {
        // given
        Member owner = memberRepository.findByHandleWithoutLogin("kky.yjj2");
        Member member = memberRepository.findByHandleWithoutLogin("mmn_mj");
        // when
        Page<Tag> tag = tagRepository.findFirstTag(owner, member, PageRequest.of(0, 1));
        // then
        assertThat(tag).isNotNull();
        assertThat(tag.hasContent()).isTrue();
        Tag firstTag = tag.getContent().get(0);
        assertThat(firstTag.getPost().getOwner()).isEqualTo(owner);
        assertThat(firstTag.getMember()).isEqualTo(member);
    }

    @Test
    @DisplayName("[추억 페이지] 함께 태그된 첫 게시물")
    void findFirstTaggedWith() {
        // given
        Member loginMember = memberRepository.findByHandleWithoutLogin("kky.yjj2");
        Member member = memberRepository.findByHandleWithoutLogin("mmn_mj");
        // when
        Page<Tag> tag = tagRepository.findFirstTaggedWith(loginMember, member, PageRequest.of(0, 1));
        // then
        assertThat(tag).isNotNull();
        assertThat(tag.hasContent()).isTrue();
        Tag firstTaggedWith = tag.getContent().get(0);
        List<Tag> tagsByPost = tagRepository.findTagsByPost(firstTaggedWith.getPost());
        boolean hasLoginMember = tagsByPost.stream().anyMatch(t -> t.getMember().equals(loginMember));
        boolean hasMember = tagsByPost.stream().anyMatch(t -> t.getMember().equals(member));
        assertThat(hasLoginMember).isTrue();
        assertThat(hasMember).isTrue();
    }
}