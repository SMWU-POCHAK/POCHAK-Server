package com.apps.pochak.comment.domain.repository;

import com.apps.pochak.block.domain.Block;
import com.apps.pochak.block.domain.repository.BlockRepository;
import com.apps.pochak.comment.domain.Comment;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.post.domain.repository.PostRepository;
import com.apps.pochak.tag.domain.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.apps.pochak.comment.fixture.CommentFixture.STATIC_CHILD_COMMENT;
import static com.apps.pochak.comment.fixture.CommentFixture.STATIC_PARENT_COMMENT;
import static com.apps.pochak.global.Constant.DEFAULT_PAGING_SIZE;
import static com.apps.pochak.member.fixture.MemberFixture.*;
import static com.apps.pochak.post.fixture.PostFixture.STATIC_PUBLIC_POST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
@SpringBootTest
class CommentRepositoryTest {

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

    @DisplayName("[부모 댓글 조회]")
    @Test
    void findParentCommentByPost() {
        //when
        Page<Comment> parentCommentByPost = commentRepository.findParentCommentByPost(post, loginMember, PageRequest.of(0, DEFAULT_PAGING_SIZE));
        //then
        assertTrue(parentCommentByPost.hasContent());
        assertEquals(parentCommentByPost.getTotalElements(), 1);
        assertEquals(parentCommentByPost.getContent().get(0), parentComment);
    }

    @DisplayName("[부모 댓글 조회] 부모 댓글 작성자 차단시")
    @Test
    void findParentCommentByPostWhenBlocked() {
        //given
        block(parentCommenter, loginMember);
        //when
        Page<Comment> parentCommentByPost = commentRepository.findParentCommentByPost(post, loginMember, PageRequest.of(0, DEFAULT_PAGING_SIZE));
        //then
        assertEquals(parentCommentByPost.getTotalElements(), 0);
    }

    @DisplayName("[자식 댓글 조회]")
    @Test
    void findChildCommentByParentComment() {
        //when
        List<Comment> childCommentByParentComment = commentRepository.findChildCommentByParentComment(parentComment, loginMember);
        //then
        assertEquals(childCommentByParentComment.size(), 1);
        assertEquals(childCommentByParentComment.get(0), childComment);
    }

    @DisplayName("[자식 댓글 조회] 자식 댓글 작성자 차단시")
    @Test
    void findChildCommentByParentCommentWhenBlocked() {
        //given
        block(childCommenter, loginMember);
        //when
        Page<Comment> parentCommentByPost = commentRepository.findParentCommentByPost(post, loginMember, PageRequest.of(0, DEFAULT_PAGING_SIZE));
        List<Comment> childCommentByParentComment = commentRepository.findChildCommentByParentComment(parentCommentByPost.getContent().get(0), loginMember);
        //then
        assertTrue(parentCommentByPost.hasContent());
        assertEquals(parentCommentByPost.getContent().get(0), parentComment);
        assertEquals(childCommentByParentComment.size(), 0);
    }

    @DisplayName("[자식 댓글 조회] 여러 부모 댓글의 자식 댓글 한번에 조회")
    @Test
    void findChildCommentByParentComments() {
        //given
        //when
        Page<Comment> parentCommentByPost = commentRepository.findParentCommentByPost(post, loginMember, PageRequest.of(0, DEFAULT_PAGING_SIZE));
        List<Comment> childCommentByParentComment = commentRepository.findChildCommentByParentComments(parentCommentByPost.stream().map(Comment::getId).toList(), loginMember.getId());
        //then
        assertTrue(parentCommentByPost.hasContent());
        assertEquals(parentCommentByPost.getContent().get(0), parentComment);
        assertEquals(childCommentByParentComment.size(), 1);
    }

    @DisplayName("[자식 댓글 조회] 여러 부모 댓글의 일부 자식 댓글 차단시 한번에 조회")
    @Test
    void findChildCommentByParentCommentsWhenBlocked() {
        //given
        block(childCommenter, loginMember);
        //when
        Page<Comment> parentCommentByPost = commentRepository.findParentCommentByPost(post, loginMember, PageRequest.of(0, DEFAULT_PAGING_SIZE));
        List<Comment> childCommentByParentComment = commentRepository.findChildCommentByParentComments(parentCommentByPost.stream().map(Comment::getId).toList(), loginMember.getId());
        //then
        assertTrue(parentCommentByPost.hasContent());
        assertEquals(parentCommentByPost.getContent().get(0), parentComment);
        assertEquals(childCommentByParentComment.size(), 0);
    }

    private void block(Member blocker, Member blockedMember) {
        Block block = Block.builder()
                .blocker(blocker)
                .blockedMember(blockedMember)
                .build();
        blockRepository.save(block);
    }
}