package com.apps.pochak.tag.domain.repository;

import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.post.domain.PostStatus;
import com.apps.pochak.post.domain.repository.PostRepository;
import com.apps.pochak.tag.domain.Tag;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static com.apps.pochak.global.util.PageUtil.getFirstContentFromPage;
import static com.apps.pochak.member.fixture.MemberFixture.*;
import static com.apps.pochak.post.fixture.PostFixture.CAPTION;
import static com.apps.pochak.post.fixture.PostFixture.POST_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class TagRepositoryTest {
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private PostRepository postRepository;

    @AfterEach
    void deleteAll() {
        postRepository.deleteAll();
        memberRepository.deleteAll();
        tagRepository.deleteAll();
    }

    @BeforeEach
    void setUp() {
        Member owner = memberRepository.save(OWNER);
        Member taggedMember1 = memberRepository.save(TAGGED_MEMBER1);
        Member taggedMember2 = memberRepository.save(TAGGED_MEMBER2);

        Post post = postRepository.save(new Post(owner, POST_IMAGE, CAPTION));
        post.makePublic();

        tagRepository.save(new Tag(post, taggedMember1));

        Post multiTagPost = postRepository.save(new Post(owner, POST_IMAGE, CAPTION));
        multiTagPost.makePublic();

        tagRepository.save(new Tag(multiTagPost, taggedMember1));
        tagRepository.save(new Tag(multiTagPost, taggedMember2));
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