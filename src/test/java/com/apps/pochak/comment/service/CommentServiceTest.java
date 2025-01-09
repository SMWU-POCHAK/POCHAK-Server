package com.apps.pochak.comment.service;

import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.block.domain.Block;
import com.apps.pochak.block.domain.repository.BlockRepository;
import com.apps.pochak.comment.domain.Comment;
import com.apps.pochak.comment.domain.repository.CommentRepository;
import com.apps.pochak.comment.dto.response.CommentElement;
import com.apps.pochak.comment.dto.response.CommentElements;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.post.domain.repository.PostRepository;
import com.apps.pochak.tag.domain.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import static com.apps.pochak.comment.fixture.CommentFixture.STATIC_CHILD_COMMENT;
import static com.apps.pochak.comment.fixture.CommentFixture.STATIC_PARENT_COMMENT;
import static com.apps.pochak.global.Constant.DEFAULT_PAGING_SIZE;
import static com.apps.pochak.member.fixture.MemberFixture.*;
import static com.apps.pochak.post.fixture.PostFixture.STATIC_PUBLIC_POST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class CommentServiceTest {

    @Autowired
    CommentService commentService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    BlockRepository blockRepository;

    @Autowired
    CommentRepository commentRepository;

    private Member loginMember;
    private Member parentCommenter;
    private Member childCommenter;
    private Post post;
    private Comment parentComment;
    private Comment childComment;

    @BeforeEach
    void setUp() {
        memberRepository.save(STATIC_MEMBER1);
        parentCommenter = memberRepository.save(STATIC_MEMBER2);
        childCommenter = memberRepository.save(STATIC_MEMBER3);
        loginMember = memberRepository.save(LOGIN_MEMBER);
        post = postRepository.save(STATIC_PUBLIC_POST);
        parentComment = commentRepository.save(STATIC_PARENT_COMMENT);
        childComment = commentRepository.save(STATIC_CHILD_COMMENT);
    }

    @Test
    void getComments() {
        CommentElement expectedChild = CommentElement.from()
                .comment(childComment)
                .build();

        CommentElements actual = commentService
                .getComments(
                        Accessor.member(loginMember.getId()),
                        post.getId(),
                        PageRequest.of(0, DEFAULT_PAGING_SIZE)
                );

        assertThat(actual.getParentCommentList()).hasSize(1);
        assertEquals(actual.getParentCommentList().get(0).getChildCommentList().size(), 1);
        assertEquals(actual.getParentCommentList().get(0).getChildCommentList().get(0), expectedChild);
    }

    @Test
    void getCommentsWhenBlockParentCommenter() {
        block(loginMember, parentCommenter);

        CommentElements actual = commentService
                .getComments(
                        Accessor.member(loginMember.getId()),
                        post.getId(),
                        PageRequest.of(0, DEFAULT_PAGING_SIZE)
                );

        assertThat(actual.getParentCommentList()).hasSize(0);
    }

    @Test
    void getCommentsWhenBlockChildCommenter() {
        block(loginMember, childCommenter);

        CommentElements actual = commentService
                .getComments(
                        Accessor.member(loginMember.getId()),
                        post.getId(),
                        PageRequest.of(0, DEFAULT_PAGING_SIZE)
                );

        assertThat(actual.getParentCommentList()).hasSize(1);
        assertEquals(actual.getParentCommentList().get(0).getCommentId(), parentComment.getId());
        assertThat(actual.getParentCommentList().get(0).getChildCommentList()).hasSize(0);
    }

    private void block(Member blocker, Member blockedMember) {
        Block block = Block.builder()
                .blocker(blocker)
                .blockedMember(blockedMember)
                .build();
        blockRepository.save(block);
    }
}