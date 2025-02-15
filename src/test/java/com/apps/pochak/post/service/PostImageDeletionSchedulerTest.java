package com.apps.pochak.post.service;

import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.global.image.CloudStorageService;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.post.domain.repository.PostRepository;
import com.apps.pochak.post.dto.request.PostUploadRequest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static com.apps.pochak.global.Constant.DEFAULT_DELETION_SIZE;
import static com.apps.pochak.global.MockMultipartFileConverter.getMockMultipartFileOfPost;
import static com.apps.pochak.member.fixture.MemberFixture.*;
import static com.apps.pochak.post.service.PostImageDeletionScheduler.EXPIRE_PERIOD;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@Transactional
@SpringBootTest
class PostImageDeletionSchedulerTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    PostService postService;

    @Autowired
    CloudStorageService storageService;

    @Autowired
    PostImageDeletionScheduler postImageDeletionScheduler;

    @Autowired
    EntityManager em;

    @MockBean
    Clock clock;

    private Member owner;
    private Member taggedMember1;
    private Member taggedMember2;

    @BeforeEach
    void setUp() {
        owner = memberRepository.save(OWNER);
        taggedMember1 = memberRepository.save(TAGGED_MEMBER1);
        taggedMember2 = memberRepository.save(TAGGED_MEMBER2);
    }

    @DisplayName("삭제된 지 1달이 지난 게시물 이미지가 삭제된다.")
    @Test
    void deletePostImage() throws Exception {
        // given
        Post post = savePublicPost();
        deletePost(post);

        LocalDateTime expiredDate = LocalDateTime.now().plusDays(EXPIRE_PERIOD);
        Clock fixedClock = Clock.fixed(
                expiredDate.atZone(ZoneId.of("Asia/Seoul")).toInstant(),
                ZoneId.of("Asia/Seoul")
        );
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());

        // when
        postImageDeletionScheduler.deleteExpiredAlarms();

        // then
        assertTrue(storageService.isObjectDeleted(post.getPostImage()));
    }

    @DisplayName("삭제된 지 1달이 지난 100개 이상의 게시물 이미지가 삭제된다.")
    @Test
    void deletePostImage_WithMoreThan100Posts() throws Exception {
        // given
        for (int i = 0; i <= DEFAULT_DELETION_SIZE; i++) {
            savePublicPost();
        }
        postRepository.deleteAll();

        List<Post> postList = postRepository.findAll();

        LocalDateTime expiredDate = LocalDateTime.now().plusDays(EXPIRE_PERIOD);
        Clock fixedClock = Clock.fixed(
                expiredDate.atZone(ZoneId.of("Asia/Seoul")).toInstant(),
                ZoneId.of("Asia/Seoul")
        );
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());

        // when
        postImageDeletionScheduler.deleteExpiredAlarms();

        // then
        assertTrue(storageService.isObjectDeleted(postList.stream().map(Post::getPostImage).toList()));
    }

    private Post savePublicPost() throws Exception {
        PostUploadRequest request = new PostUploadRequest(
                getMockMultipartFileOfPost(),
                "test caption",
                List.of(taggedMember1.getHandle(), taggedMember2.getHandle())
        );

        postService.savePost(
                Accessor.member(owner.getId()),
                request
        );
        Post post = postRepository.findAll().get(0);
        post.makePublic();

        return post;
    }

    private void deletePost(final Post post) {
        postService.deletePost(
                Accessor.member(owner.getId()),
                post.getId()
        );
    }
}