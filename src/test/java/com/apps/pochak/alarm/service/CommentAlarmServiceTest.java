package com.apps.pochak.alarm.service;

import com.apps.pochak.alarm.domain.Alarm;
import com.apps.pochak.alarm.domain.repository.AlarmRepository;
import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.comment.domain.Comment;
import com.apps.pochak.comment.domain.repository.CommentRepository;
import com.apps.pochak.comment.dto.request.CommentUploadRequest;
import com.apps.pochak.comment.service.CommentService;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.post.domain.repository.PostRepository;
import com.apps.pochak.post.dto.request.PostUploadRequest;
import com.apps.pochak.post.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.apps.pochak.alarm.domain.AlarmType.*;
import static com.apps.pochak.global.MockMultipartFileConverter.getMockMultipartFileOfPost;
import static com.apps.pochak.member.fixture.MemberFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class CommentAlarmServiceTest {

    @Autowired
    CommentService commentService;

    @Autowired
    PostService postService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    AlarmRepository alarmRepository;

    @Autowired
    CommentRepository commentRepository;

    private Member owner;
    private Member taggedMember1;
    private Member taggedMember2;
    private Member loginMember;
    private Member member;

    @BeforeEach
    void setUp() {
        owner = memberRepository.save(OWNER);
        taggedMember1 = memberRepository.save(TAGGED_MEMBER1);
        taggedMember2 = memberRepository.save(TAGGED_MEMBER2);
        loginMember = memberRepository.save(LOGIN_MEMBER);
        member = memberRepository.save(MEMBER);
    }

    @DisplayName("부모 댓글 알람이 성공적으로 저장된다.")
    @Test
    void saveParentCommentAlarm() throws Exception {
        // given
        Post post = savePublicPost();

        // when
        saveParentComment(post);

        // then
        List<Alarm> alarmList = alarmRepository.findAll();
        Alarm ownerAlarm = alarmList.stream()
                .filter(alarm -> alarm.getReceiver().equals(owner))
                .findFirst()
                .orElseThrow(() -> new AssertionError("알람 저장 실패"));
        List<Alarm> taggedMemberAlarmList = alarmList.stream()
                .filter(alarm -> !alarm.getReceiver().equals(owner))
                .toList();

        assertAll(
                () -> assertEquals(3, alarmList.size()),
                () -> assertEquals(OWNER_COMMENT, ownerAlarm.getAlarmType()),
                () -> assertThat(taggedMemberAlarmList)
                        .extracting(Alarm::getReceiver)
                        .containsExactlyInAnyOrder(taggedMember1, taggedMember2),
                () -> assertThat(taggedMemberAlarmList)
                        .extracting(Alarm::getAlarmType)
                        .containsOnly(TAGGED_COMMENT)
        );
    }

    @DisplayName("게시물 주인이 부모 댓글을 달았을 시 작성자에게 알람을 보내지 않는다.")
    @Test
    void saveParentCommentAlarm_WhenWriterIsOwner() throws Exception {
        // given
        Post post = savePublicPost();

        // when
        saveParentComment(post, Accessor.member(owner.getId()));

        // then
        List<Alarm> alarmList = alarmRepository.findAll();

        assertAll(
                () -> assertEquals(2, alarmList.size()),
                () -> assertThat(alarmList)
                        .extracting(Alarm::getReceiver)
                        .containsExactlyInAnyOrder(taggedMember1, taggedMember2),
                () -> assertThat(alarmList)
                        .extracting(Alarm::getAlarmType)
                        .containsOnly(TAGGED_COMMENT)
        );
    }

    @DisplayName("태그된 사람이 부모 댓글을 달았을 시 작성자에게 알림을 보내지 않는다.")
    @Test
    void saveParentCommentAlarm_WhenWriterIsTaggedMember() throws Exception {
        // given
        Post post = savePublicPost();

        // when
        saveParentComment(post, Accessor.member(taggedMember1.getId()));

        // then
        List<Alarm> alarmList = alarmRepository.findAll();
        Alarm ownerAlarm = alarmList.stream()
                .filter(alarm -> alarm.getReceiver().equals(owner))
                .findFirst()
                .orElseThrow(() -> new AssertionError("알람 저장 실패"));
        Alarm taggedMemberAlarm = alarmList.stream()
                .filter(alarm -> alarm.getReceiver().equals(taggedMember2))
                .findFirst()
                .orElseThrow(() -> new AssertionError("알람 저장 실패"));

        assertAll(
                () -> assertEquals(2, alarmList.size()),
                () -> assertEquals(OWNER_COMMENT, ownerAlarm.getAlarmType()),
                () -> assertEquals(TAGGED_COMMENT, taggedMemberAlarm.getAlarmType())
        );
    }

    @DisplayName("자식 댓글 알람이 성공적으로 저장된다.")
    @Test
    void saveChildCommentAlarm() throws Exception {
        // given
        Post post = savePublicPost();
        Comment comment = saveParentComment(post, Accessor.member(member.getId()));
        alarmRepository.deleteAll();

        // when
        saveChildComment(post, comment, Accessor.member(loginMember.getId()));

        // then
        List<Alarm> alarmList = alarmRepository.findAll();
        Alarm ownerAlarm = alarmList.stream()
                .filter(alarm -> alarm.getReceiver().equals(owner))
                .findFirst()
                .orElseThrow(() -> new AssertionError("알람 저장 실패"));
        List<Alarm> taggedMemberAlarmList = alarmList.stream()
                .filter(alarm -> alarm.getReceiver().equals(taggedMember1) || alarm.getReceiver().equals(taggedMember2))
                .toList();
        Alarm replyAlarm = alarmList.stream()
                .filter(alarm -> alarm.getReceiver().equals(member))
                .findFirst()
                .orElseThrow(() -> new AssertionError("알람 저장 실패"));

        assertAll(
                () -> assertEquals(4, alarmList.size()),
                () -> assertEquals(OWNER_COMMENT, ownerAlarm.getAlarmType()),
                () -> assertThat(taggedMemberAlarmList)
                        .extracting(Alarm::getReceiver)
                        .containsExactlyInAnyOrder(taggedMember1, taggedMember2),
                () -> assertThat(taggedMemberAlarmList)
                        .extracting(Alarm::getAlarmType)
                        .containsOnly(TAGGED_COMMENT),
                () -> assertEquals(COMMENT_REPLY, replyAlarm.getAlarmType())
        );
    }

    @DisplayName("자신의 댓글에 답글을 달았을 시 작성자에게 알림을 보내지 않는다.")
    @Test
    void saveChildCommentAlarm_WhenReplyOneself() throws Exception {
        // given
        Post post = savePublicPost();
        Comment comment = saveParentComment(post, Accessor.member(member.getId()));
        alarmRepository.deleteAll();

        // when
        saveChildComment(post, comment, Accessor.member(member.getId()));

        // then
        List<Alarm> alarmList = alarmRepository.findAll();
        Alarm ownerAlarm = alarmList.stream()
                .filter(alarm -> alarm.getReceiver().equals(owner))
                .findFirst()
                .orElseThrow(() -> new AssertionError("알람 저장 실패"));
        List<Alarm> taggedMemberAlarmList = alarmList.stream()
                .filter(alarm -> !alarm.getReceiver().equals(owner))
                .toList();
        assertAll(
                () -> assertEquals(3, alarmList.size()),
                () -> assertEquals(OWNER_COMMENT, ownerAlarm.getAlarmType()),
                () -> assertThat(taggedMemberAlarmList)
                        .extracting(Alarm::getReceiver)
                        .containsExactlyInAnyOrder(taggedMember1, taggedMember2),
                () -> assertThat(taggedMemberAlarmList)
                        .extracting(Alarm::getAlarmType)
                        .containsOnly(TAGGED_COMMENT)
        );
    }

    @DisplayName("게시글 주인의 댓글에 답글을 달았을 시 답글 알림만 전송한다.")
    @Test
    void saveChildCommentAlarm_WhenReplyToOwner() throws Exception {
        // given
        Post post = savePublicPost();
        Comment comment = saveParentComment(post, Accessor.member(owner.getId()));
        alarmRepository.deleteAll();

        // when
        saveChildComment(post, comment, Accessor.member(loginMember.getId()));

        // then
        List<Alarm> alarmList = alarmRepository.findAll();
        Alarm replyAlarm = alarmList.stream()
                .filter(alarm -> alarm.getReceiver().equals(owner))
                .findFirst()
                .orElseThrow(() -> new AssertionError("알람 저장 실패"));
        List<Alarm> taggedMemberAlarmList = alarmList.stream()
                .filter(alarm -> !alarm.getReceiver().equals(owner))
                .toList();

        assertAll(
                () -> assertEquals(3, alarmList.size()),
                () -> assertEquals(COMMENT_REPLY, replyAlarm.getAlarmType()),
                () -> assertThat(taggedMemberAlarmList)
                        .extracting(Alarm::getReceiver)
                        .containsExactlyInAnyOrder(taggedMember1, taggedMember2),
                () -> assertThat(taggedMemberAlarmList)
                        .extracting(Alarm::getAlarmType)
                        .containsOnly(TAGGED_COMMENT)
        );
    }

    @DisplayName("태그된 멤버의 댓글에 답글을 달았을 시 답글 알림만 전송한다.")
    @Test
    void saveChildCommentAlarm_WhenReplyToTaggedMember() throws Exception {
        // given
        Post post = savePublicPost();
        Comment comment = saveParentComment(post, Accessor.member(taggedMember1.getId()));
        alarmRepository.deleteAll();

        // when
        saveChildComment(post, comment, Accessor.member(loginMember.getId()));

        // then
        List<Alarm> alarmList = alarmRepository.findAll();
        Alarm replyAlarm = alarmList.stream()
                .filter(alarm -> alarm.getReceiver().equals(taggedMember1))
                .findFirst()
                .orElseThrow(() -> new AssertionError("알람 저장 실패"));
        Alarm ownerAlarm = alarmList.stream()
                .filter(alarm -> alarm.getReceiver().equals(owner))
                .findFirst()
                .orElseThrow(() -> new AssertionError("알람 저장 실패"));
        Alarm taggedMemberAlarm = alarmList.stream()
                .filter(alarm -> alarm.getReceiver().equals(taggedMember2))
                .findFirst()
                .orElseThrow(() -> new AssertionError("알람 저장 실패"));

        assertAll(
                () -> assertEquals(3, alarmList.size()),
                () -> assertEquals(COMMENT_REPLY, replyAlarm.getAlarmType()),
                () -> assertEquals(OWNER_COMMENT, ownerAlarm.getAlarmType()),
                () -> assertEquals(TAGGED_COMMENT, taggedMemberAlarm.getAlarmType())
        );
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

        alarmRepository.deleteAll();
        return post;
    }

    private Comment saveParentComment(
            final Post post
    ) {
        return saveParentComment(post, Accessor.member(loginMember.getId()));
    }

    private Comment saveParentComment(
            final Post post,
            final Accessor accessor
    ) {
        CommentUploadRequest request = new CommentUploadRequest(
                "부모 댓글 작성",
                null
        );

        commentService.saveComment(
                accessor,
                post.getId(),
                request
        );

        return commentRepository.findAll()
                .stream()
                .filter(comment -> comment.getParentComment() == null)
                .findFirst()
                .get();
    }

    private Comment saveChildComment(
            final Post post,
            final Comment parentComment,
            final Accessor accessor
    ) {
        CommentUploadRequest request = new CommentUploadRequest(
                "자식 댓글 작성",
                parentComment.getId()
        );

        commentService.saveComment(
                accessor,
                post.getId(),
                request
        );

        return commentRepository.findAll()
                .stream()
                .filter(comment -> comment.getParentComment() != null)
                .findFirst()
                .get();
    }
}