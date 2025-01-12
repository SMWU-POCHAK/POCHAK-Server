package com.apps.pochak.tag.service;

import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.global.ServiceTest;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.post.domain.repository.PostRepository;
import com.apps.pochak.post.dto.request.PostUploadRequest;
import com.apps.pochak.post.service.PostService;
import com.apps.pochak.tag.domain.Tag;
import com.apps.pochak.tag.domain.repository.TagRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.apps.pochak.global.BaseEntityStatus.DELETED;
import static com.apps.pochak.global.MockMultipartFileConverter.getMockMultipartFileOfPost;
import static com.apps.pochak.member.fixture.MemberFixture.*;
import static com.apps.pochak.post.domain.PostStatus.PUBLIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class TagServiceTest extends ServiceTest {

    @Autowired
    TagService tagService;

    @Autowired
    PostService postService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    EntityManager em;

    private Member owner;
    private Member taggedMember1;
    private Member taggedMember2;

    @BeforeEach
    void setUp() {
        owner = memberRepository.save(OWNER);
        taggedMember1 = memberRepository.save(TAGGED_MEMBER1);
        taggedMember2 = memberRepository.save(TAGGED_MEMBER2);
    }

    @DisplayName("[태그 수락] 태그가 정상적으로 수락된다.")
    @Test
    void approveTag() throws Exception {
        // given
        Post post = savePost();

        // when
        Tag tag = tagRepository.findAll().get(0);
        tagService.approveOrRejectTagRequest(
                Accessor.member(taggedMember1.getId()),
                tag.getId(),
                true
        );

        em.flush();
        em.clear();

        // then
        Tag savedTag = tagRepository.findAll().get(0);
        assertEquals(true, savedTag.getIsAccepted());
    }

    @DisplayName("[태그 수락] 모두 태그를 수락하면 게시물이 공개된다.")
    @Test
    void approveTag_whenAllAllowed() throws Exception {
        // given
        savePost();

        // when
        tagService.approveOrRejectTagRequest(
                Accessor.member(taggedMember1.getId()),
                tagRepository.findTagByMember(taggedMember1)
                        .orElseThrow(() -> new AssertionError("태그 저장 실패"))
                        .getId(),
                true
        );

        tagService.approveOrRejectTagRequest(
                Accessor.member(taggedMember2.getId()),
                tagRepository.findTagByMember(taggedMember2)
                        .orElseThrow(() -> new AssertionError("태그 저장 실패"))
                        .getId(),
                true
        );

        em.flush();
        em.clear();

        // then
        List<Tag> tagList = tagRepository.findAll();
        Post post = postRepository.findAll().get(0);
        assertAll(
                () -> assertThat(tagList)
                        .extracting(Tag::getIsAccepted)
                        .containsOnly(true),
                () -> assertEquals(PUBLIC, post.getPostStatus())
        );
    }

    @DisplayName("[태그 거절] 태그를 거절하면 게시물이 삭제된다.")
    @Test
    void rejectTag() throws Exception {
        // given
        savePost();

        // when
        tagService.approveOrRejectTagRequest(
                Accessor.member(taggedMember1.getId()),
                tagRepository.findTagByMember(taggedMember1)
                        .orElseThrow(() -> new AssertionError("태그 저장 실패"))
                        .getId(),
                false
        );

        em.flush();
        em.clear();

        // then
        List<Tag> tagList = tagRepository.findAll();
        Post post = postRepository.findAll().get(0);
        assertAll(
                () -> assertEquals(0, tagList.size()),
                () -> assertEquals(DELETED, post.getStatus())
        );
    }

    private Post savePost() throws Exception {
        PostUploadRequest request = new PostUploadRequest(
                getMockMultipartFileOfPost(),
                "test caption",
                List.of(taggedMember1.getHandle(), taggedMember2.getHandle())
        );

        postService.savePost(
                Accessor.member(owner.getId()),
                request
        );

        return postRepository.findAll().get(0);
    }
}