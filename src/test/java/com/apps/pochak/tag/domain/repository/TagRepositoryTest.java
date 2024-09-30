package com.apps.pochak.tag.domain.repository;

import com.apps.pochak.global.config.JpaAuditingConfig;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.post.domain.PostStatus;
import com.apps.pochak.post.domain.repository.PostRepository;
import com.apps.pochak.tag.domain.Tag;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static com.apps.pochak.global.util.PageUtil.getFirstContentFromPage;
import static com.apps.pochak.member.fixture.MemberFixture.*;
import static com.apps.pochak.post.fixture.PostFixture.POST_WITH_MULTI_TAG;
import static com.apps.pochak.post.fixture.PostFixture.PUBLIC_POST;
import static com.apps.pochak.tag.fixture.TagFixture.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
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
        memberRepository.save(OWNER);
        memberRepository.save(TAGGED_MEMBER1);
        memberRepository.save(TAGGED_MEMBER2);

        PUBLIC_POST.makePublic();
        postRepository.save(PUBLIC_POST);;
        tagRepository.save(APPROVAL_TAG);

        POST_WITH_MULTI_TAG.makePublic();
        postRepository.save(POST_WITH_MULTI_TAG);

        tagRepository.save(TAG1_WITH_ONE_POST);
        tagRepository.save(TAG2_WITH_ONE_POST);
    }

    @Test
    @DisplayName("[추억 페이지] 친구를 태그한 첫 게시물을 조회한다.")
    void findFirstTaggedPost() {
        // given
        Member owner = memberRepository.findByHandleWithoutLogin("owner");
        Member member = memberRepository.findByHandleWithoutLogin("tagged_member1");
        // when
        Page<Tag> tag = tagRepository.findTagByOwnerAndMember(owner, member, PageRequest.of(0, 1));
        // then
        assertThat(tag).isNotNull();
        assertThat(tag.hasContent()).isTrue();
        Tag firstTag = getFirstContentFromPage(tag);
        assertThat(firstTag.getPost().getOwner()).isEqualTo(owner);
        assertThat(firstTag.getMember()).isEqualTo(member);
    }

    @Test
    @DisplayName("[추억 페이지] 함께 태그된 첫 게시물을 조회한다.")
    void findTaggedWith() {
        // given
        Member loginMember = memberRepository.findByHandleWithoutLogin("tagged_member1");
        Member member = memberRepository.findByHandleWithoutLogin("tagged_member2");
        // when
        Page<Tag> tag = tagRepository.findTaggedWith(loginMember, member, PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "post.allowedDate")));
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

    @Test
    @DisplayName("[추억 페이지] 가장 최근 게시물을 조회한다.")
    void findLatestTag() {
        // given
        Member loginMember = memberRepository.findByHandleWithoutLogin("tagged_member1");
        Member member = memberRepository.findByHandleWithoutLogin("tagged_member2");
        // when
        Page<Tag> taggedWithDesc = tagRepository.findTaggedWith(loginMember, member, PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "post.allowedDate")));
        Page<Tag> tagOrTagged = tagRepository.findLatestTagged(loginMember, member, PageRequest.of(0, 1));

        Tag latestTaggedWith = (taggedWithDesc.hasContent() ? taggedWithDesc.getContent().get(0) : null);
        Tag latestTagOrTagged = (tagOrTagged.hasContent() ? tagOrTagged.getContent().get(0) : null);

        Tag latestTag = null;
        if (latestTaggedWith != null && latestTagOrTagged != null) {
            latestTag = latestTaggedWith.getPost().getAllowedDate().isAfter(latestTagOrTagged.getPost().getAllowedDate()) ? latestTaggedWith : latestTagOrTagged;
        } else if (latestTaggedWith == null && latestTagOrTagged != null) {
            latestTag = latestTagOrTagged;
        } else if (latestTaggedWith != null) {
            latestTag = latestTaggedWith;
        }
        // then
        assertThat(latestTag).isNotNull();
    }

    @Test
    @DisplayName("[추억 페이지] 함께 태그된 게시물의 수를 조회한다.")
    void countByMember() {
        Member loginMember = memberRepository.findByHandleWithoutLogin("tagged_member1");
        Member member = memberRepository.findByHandleWithoutLogin("tagged_member2");

        Page<Tag> taggedWith = tagRepository.findTaggedWith(loginMember, member, PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "post.allowedDate")));
        Long count = tagRepository.countByMember(loginMember, member);
        assertThat(count).isEqualTo(taggedWith.getTotalElements());
    }

    @Test
    @DisplayName("[추억 페이지] 내가 태그한, 태그된 게시물의 수를 조회한다.")
    void countByPostOwner() {
        Member owner = memberRepository.findByHandleWithoutLogin("owner");
        Member member = memberRepository.findByHandleWithoutLogin("tagged_member1");

        Page<Tag> tag = tagRepository.findTagByOwnerAndMember(owner, member, PageRequest.of(0, 1));
        Long count = tagRepository.countByPost_PostStatusAndPost_OwnerAndMember(PostStatus.PUBLIC, owner, member);
        assertThat(count).isEqualTo(tag.getTotalElements());
    }
}