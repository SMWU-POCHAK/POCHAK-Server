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
import com.apps.pochak.post.fixture.PostFixture;
import com.apps.pochak.tag.domain.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import static com.apps.pochak.global.Constant.COMMENT_PAGING_SIZE;
import static com.apps.pochak.member.fixture.MemberFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class CommentServiceTest {

    private static final Logger log = LoggerFactory.getLogger(CommentServiceTest.class);
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
    private Comment childComment2;

    @BeforeEach
    void setUp() {
        post = savePost(memberRepository.save(OWNER));
        parentCommenter = memberRepository.save(PARENT_COMMENTER);
        childCommenter = memberRepository.save(CHILD_COMMENTER);
        loginMember = memberRepository.save(LOGIN_MEMBER);
        parentComment = saveParentComment(parentCommenter, post);
        childComment = saveChildComment(childCommenter, post, parentComment);
    }

    @Test
    @DisplayName("[전체 댓글 조회] 정상 테스트")
    void getComments() {
        // given
        childComment2 = saveChildComment(childCommenter, post, parentComment);

        CommentElement expectedChild = CommentElement.from()
                .comment(childComment2)
                .build();
        // then
        CommentElements actual = commentService
                .getComments(
                        Accessor.member(loginMember.getId()),
                        post.getId(),
                        PageRequest.of(0, COMMENT_PAGING_SIZE, Sort.by(Sort.Direction.DESC, "createdDate"))
                );
        // then
        assertAll(
                () -> assertThat(actual.getParentCommentList()).hasSize(1),
                () -> assertEquals(1, actual.getParentCommentList().get(0).getChildCommentList().size()),
                () -> assertEquals(expectedChild, actual.getParentCommentList().get(0).getChildCommentList().get(0))
        );
    }

    @Test
    @DisplayName("[전체 댓글 조회] 부모 댓글 작성자 차단시")
    void getCommentsWhenBlockParentCommenter() {
        //given
        block(loginMember, parentCommenter);
        // when
        CommentElements actual = commentService
                .getComments(
                        Accessor.member(loginMember.getId()),
                        post.getId(),
                        PageRequest.of(0, COMMENT_PAGING_SIZE, Sort.by(Sort.Direction.ASC, "createdDate"))
                );
        // then
        assertThat(actual.getParentCommentList()).isEmpty();
    }

    @Test
    @DisplayName("[전체 댓글 조회] 부모 댓글 작성자가 차단시")
    void getCommentsWhenBlockedByParentCommenter() {
        //given
        block(parentCommenter, loginMember);
        // when
        CommentElements actual = commentService
                .getComments(
                        Accessor.member(loginMember.getId()),
                        post.getId(),
                        PageRequest.of(0, COMMENT_PAGING_SIZE, Sort.by(Sort.Direction.ASC, "createdDate"))
                );
        // then
        assertThat(actual.getParentCommentList()).isEmpty();
    }

    @Test
    @DisplayName("[전체 댓글 조회] 자식 댓글 작성자 차단시")
    void getCommentsWhenBlockChildCommenter() {
        //given
        block(loginMember, childCommenter);
        // when
        CommentElements actual = commentService
                .getComments(
                        Accessor.member(loginMember.getId()),
                        post.getId(),
                        PageRequest.of(0, COMMENT_PAGING_SIZE, Sort.by(Sort.Direction.ASC, "createdDate"))
                );
        // then
        assertAll(
                () -> assertThat(actual.getParentCommentList()).hasSize(1),
                () -> assertEquals(parentComment.getId(), actual.getParentCommentList().get(0).getCommentId()),
                () -> assertThat(actual.getParentCommentList().get(0).getChildCommentList()).isEmpty()
        );
    }

    @Test
    @DisplayName("[전체 댓글 조회] 자식 댓글 작성자가 차단시")
    void getCommentsWhenBlockedByChildCommenter() {
        //given
        block(childCommenter, loginMember);
        // when
        CommentElements actual = commentService
                .getComments(
                        Accessor.member(loginMember.getId()),
                        post.getId(),
                        PageRequest.of(0, COMMENT_PAGING_SIZE, Sort.by(Sort.Direction.ASC, "createdDate"))
                );
        // then
        assertAll(
                () -> assertThat(actual.getParentCommentList()).hasSize(1),
                () -> assertEquals(parentComment.getId(), actual.getParentCommentList().get(0).getCommentId()),
                () -> assertThat(actual.getParentCommentList().get(0).getChildCommentList()).isEmpty()
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