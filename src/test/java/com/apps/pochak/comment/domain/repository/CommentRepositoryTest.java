package com.apps.pochak.comment.domain.repository;

import com.apps.pochak.block.domain.Block;
import com.apps.pochak.block.domain.repository.BlockRepository;
import com.apps.pochak.comment.domain.Comment;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.post.domain.repository.PostRepository;
import com.apps.pochak.post.fixture.PostFixture;
import com.apps.pochak.tag.domain.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.apps.pochak.global.Constant.DEFAULT_PAGING_SIZE;
import static com.apps.pochak.member.fixture.MemberFixture.*;
import static org.junit.jupiter.api.Assertions.*;

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
        post = savePost(memberRepository.save(OWNER));
        parentCommenter = memberRepository.save(PARENT_COMMENTER);
        childCommenter = memberRepository.save(CHILD_COMMENTER);
        loginMember = memberRepository.save(LOGIN_MEMBER);
        parentComment = saveParentComment(parentCommenter, post);
        childComment = saveChildComment(childCommenter, post, parentComment);
    }

    @DisplayName("[전체 댓글 조회] 부모 댓글과 자식 댓글")
    @Test
    void findFirstChildCommentByParentComments() {
        //given
        //when
        Page<Comment> parentCommentByPost = commentRepository.findParentCommentByPost(post, loginMember, PageRequest.of(0, DEFAULT_PAGING_SIZE));
        List<Comment> childCommentByParentComment = commentRepository.findFirstChildCommentByParentComments(parentCommentByPost.stream().map(Comment::getId).toList(), loginMember.getId());
        //then
        assertAll(
                () -> assertTrue(parentCommentByPost.hasContent()),
                () -> assertEquals(parentCommentByPost.getContent().get(0), parentComment),
                () -> assertEquals(1, childCommentByParentComment.size())
        );
    }

    @DisplayName("[전체 댓글 조회] 부모 댓글의 특정 자식 댓글 차단시")
    @Test
    void findFirstChildCommentByParentCommentsWhenBlocked() {
        //given
        block(childCommenter, loginMember);
        //when
        Page<Comment> parentCommentByPost = commentRepository.findParentCommentByPost(
                post, loginMember, PageRequest.of(0, DEFAULT_PAGING_SIZE)
        );
        List<Comment> childCommentByParentComment = commentRepository.findFirstChildCommentByParentComments(
                parentCommentByPost.stream().map(Comment::getId).toList(), loginMember.getId()
        );

        //then
        assertAll(
                () -> assertTrue(parentCommentByPost.hasContent()),
                () -> assertEquals(parentCommentByPost.getContent().get(0), parentComment),
                () -> assertEquals(0, childCommentByParentComment.size())
        );
    }

    @DisplayName("[부모 댓글 조회] 정상 테스트")
    @Test
    void findParentCommentByPost() {
        //when
        Page<Comment> parentCommentByPost = commentRepository.findParentCommentByPost(
                post, loginMember, PageRequest.of(0, DEFAULT_PAGING_SIZE)
        );

        //then
        assertAll(
                () -> assertTrue(parentCommentByPost.hasContent()),
                () -> assertEquals(parentCommentByPost.getTotalElements(), 1),
                () -> assertEquals(parentComment, parentCommentByPost.getContent().get(0))
        );
    }

    @DisplayName("[부모 댓글 조회] 부모 댓글 작성자 차단시")
    @Test
    void findParentCommentByPostWhenBlocked() {
        //given
        block(parentCommenter, loginMember);
      
        //when
        Page<Comment> parentCommentByPost = commentRepository.findParentCommentByPost(post, loginMember, PageRequest.of(0, DEFAULT_PAGING_SIZE));

        //then
        assertEquals(0, parentCommentByPost.getTotalElements());
    }

    @DisplayName("[자식 댓글 조회] 정상 테스트")
    @Test
    void findChildCommentByParentComment() {
        //when
        Page<Comment> childCommentByParentComment = commentRepository.findChildCommentByParentComment(
                parentComment, loginMember, PageRequest.of(0, DEFAULT_PAGING_SIZE, Sort.by(Sort.Direction.DESC, "createdDate")));
        //then
        assertEquals(childCommentByParentComment.getContent().size(), 1);
        assertEquals(childComment, childCommentByParentComment.getContent().get(0));
    }

    @DisplayName("[자식 댓글 조회] 자식 댓글 작성자 차단시")
    @Test
    void findChildCommentByParentCommentWhenBlocked() {
        //given
        block(childCommenter, loginMember);

        //when
        Page<Comment> parentCommentByPost = commentRepository.findParentCommentByPost(
                post, loginMember, PageRequest.of(0, DEFAULT_PAGING_SIZE)
        );
        Page<Comment> childCommentByParentComment = commentRepository.findChildCommentByParentComment(
                parentCommentByPost.getContent().get(0),
                loginMember,
                PageRequest.of(0, DEFAULT_PAGING_SIZE)
        );

        //then
        assertAll(
                () -> assertTrue(parentCommentByPost.hasContent()),
                () -> assertEquals(parentCommentByPost.getContent().get(0), parentComment),
                () -> assertEquals(0, childCommentByParentComment.getContent().size())
        );
    }

    private Post savePost(Member owner) {
        Post post = postRepository.save(PostFixture.get(owner));
        post.makePublic();
        return post;
    }

    private Comment saveParentComment(Member member, Post post) {
        return commentRepository.save(new Comment("부모 댓글 입니다.", member, post));
    }

    private Comment saveChildComment(Member member, Post post, Comment parentComment) {
        return commentRepository.save(new Comment("자식 댓글 입니다.", member, post, parentComment));
    }

    private void block(Member blocker, Member blockedMember) {
        Block block = Block.builder()
                .blocker(blocker)
                .blockedMember(blockedMember)
                .build();
        blockRepository.save(block);
    }
}