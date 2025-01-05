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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.apps.pochak.global.Constant.DEFAULT_PAGING_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    private Member loginMember;
    private Member parentCommenter;
    private Member childCommenter;
    private Post post;
    private Comment parentComment;
    private Comment childComment;


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

    @BeforeEach
    void setUp() {
        memberRepository.save(POST_OWNER);
        parentCommenter = memberRepository.save(PARENT_COMMENTER);
        childCommenter = memberRepository.save(CHILD_COMMENTER);
        loginMember = memberRepository.save(LOGIN_MEMBER);
        post = postRepository.save(POST);
        parentComment = commentRepository.save(PARENT_COMMENT);
        childComment = commentRepository.save(CHILD_COMMENT);
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
    void findChildCommentByParentCommentsWhenBlocked() {
        //given
        //when
        Page<Comment> parentCommentByPost = commentRepository.findParentCommentByPost(post, loginMember, PageRequest.of(0, DEFAULT_PAGING_SIZE));
        List<Comment> childCommentByParentComment = commentRepository.findChildCommentByParentComments(parentCommentByPost.stream().map(Comment::getId).toList(), loginMember.getId());
        //then
        assertTrue(parentCommentByPost.hasContent());
        assertEquals(parentCommentByPost.getContent().get(0), parentComment);
        assertEquals(childCommentByParentComment.size(), 1);
    }

    private void block(Member blocker, Member blockedMember) {
        Block block = Block.builder()
                .blocker(blocker)
                .blockedMember(blockedMember)
                .build();
        blockRepository.save(block);
    }
}