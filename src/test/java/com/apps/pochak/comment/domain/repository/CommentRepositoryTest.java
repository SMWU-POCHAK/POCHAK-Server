package com.apps.pochak.comment.domain.repository;

import com.apps.pochak.block.domain.Block;
import com.apps.pochak.block.domain.repository.BlockRepository;
import com.apps.pochak.comment.domain.Comment;
import com.apps.pochak.comment.fixture.CommentFixture;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.member.fixture.MemberFixture;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.post.domain.repository.PostRepository;
import com.apps.pochak.post.fixture.PostFixture;
import com.apps.pochak.tag.domain.repository.TagRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.apps.pochak.global.Constant.DEFAULT_PAGING_SIZE;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class CommentRepositoryTest {

    private static final Member LOGIN_MEMBER = MemberFixture.LOGIN_MEMBER;
    private static final Member POST_OWNER = MemberFixture.STATIC_MEMBER1;
    private static final Post POST = PostFixture.STATIC_PUBLIC_POST;
    private static final Member PARENT_COMMENTER = MemberFixture.STATIC_MEMBER2;
    private static final Member CHILD_COMMENTER = MemberFixture.STATIC_MEMBER3;
    private static final Comment PARENT_COMMENT = CommentFixture.STATIC_PARENT_COMMENT;
    private static final Comment CHILD_COMMENT = CommentFixture.STATIC_CHILD_COMMENT;

    @Autowired
    EntityManager em;

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

    @AfterEach
    void deleteAll() {
        postRepository.deleteAll();
        memberRepository.deleteAll();
        tagRepository.deleteAll();
        blockRepository.deleteAll();
    }

    @DisplayName("[부모 댓글 조회]")
    @Test
    void findParentCommentByPost() {
        //given
        memberRepository.save(POST_OWNER);
        memberRepository.save(PARENT_COMMENTER);
        memberRepository.save(CHILD_COMMENTER);
        Member loginMember = memberRepository.save(LOGIN_MEMBER);

        Post post = postRepository.save(POST);
        Comment parentComment = commentRepository.save(PARENT_COMMENT);
        commentRepository.save(CHILD_COMMENT);

        Page<Comment> parentCommentByPost = commentRepository.findParentCommentByPost(post, loginMember, PageRequest.of(0, DEFAULT_PAGING_SIZE));
        assertTrue(parentCommentByPost.hasContent());
        assertEquals(parentCommentByPost.getTotalElements(), 1);
        assertEquals(parentCommentByPost.getContent().get(0), parentComment);
    }

    @DisplayName("[부모 댓글 조회] 부모 댓글 작성자 차단시")
    @Test
    void findParentCommentByPostWhenBlocked() {
        //given
        memberRepository.save(POST_OWNER);
        Member parentCommenter = memberRepository.save(PARENT_COMMENTER);
        memberRepository.save(CHILD_COMMENTER);
        Member loginMember = memberRepository.save(LOGIN_MEMBER);

        Post post = postRepository.save(POST);
        commentRepository.save(PARENT_COMMENT);
        commentRepository.save(CHILD_COMMENT);

        block(parentCommenter, loginMember);

        Page<Comment> parentCommentByPost = commentRepository.findParentCommentByPost(post, loginMember, PageRequest.of(0, DEFAULT_PAGING_SIZE));
        assertEquals(parentCommentByPost.getTotalElements(), 0);
    }

    @DisplayName("[자식 댓글 조회]")
    @Test
    void findChildCommentByParentComment() {
        //given
        memberRepository.save(POST_OWNER);
        memberRepository.save(PARENT_COMMENTER);
        memberRepository.save(CHILD_COMMENTER);
        Member loginMember = memberRepository.save(LOGIN_MEMBER);

        postRepository.save(POST);
        Comment parentComment = commentRepository.save(PARENT_COMMENT);
        Comment childComment = commentRepository.save(CHILD_COMMENT);

        List<Comment> childCommentByParentComment = commentRepository.findChildCommentByParentComment(parentComment, loginMember);
        assertEquals(childCommentByParentComment.size(), 1);
        assertEquals(childCommentByParentComment.get(0), childComment);
    }

    @DisplayName("[자식 댓글 조회] 자식 댓글 작성자 차단시")
    @Test
    void findChildCommentByParentCommentWhenBlocked() {
        //given
        memberRepository.save(POST_OWNER);
        memberRepository.save(PARENT_COMMENTER);
        Member childCommenter = memberRepository.save(CHILD_COMMENTER);
        Member loginMember = memberRepository.save(LOGIN_MEMBER);

        Post post = postRepository.save(POST);
        Comment parentComment = commentRepository.save(PARENT_COMMENT);
        commentRepository.save(CHILD_COMMENT);

        block(childCommenter, loginMember);

        Page<Comment> parentCommentByPost = commentRepository.findParentCommentByPost(post, loginMember, PageRequest.of(0, DEFAULT_PAGING_SIZE));
        assertTrue(parentCommentByPost.hasContent());
        assertEquals(parentCommentByPost.getContent().get(0), parentComment);
        List<Comment> childCommentByParentComment = commentRepository.findChildCommentByParentComment(parentCommentByPost.getContent().get(0), loginMember);
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