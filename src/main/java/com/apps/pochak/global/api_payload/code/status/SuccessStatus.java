package com.apps.pochak.global.api_payload.code.status;

import com.apps.pochak.global.api_payload.code.BaseCode;
import com.apps.pochak.global.api_payload.code.ReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@AllArgsConstructor
public enum SuccessStatus implements BaseCode {
    // common
    _OK(OK, "COMMON200", "성공입니다."),

    // Alarm
    SUCCESS_CHECK_ALARM(NO_CONTENT, "ALARM2001", "성공적으로 알람을 확인하였습니다."),

    // Block
    SUCCESS_BLOCK_MEMBER(CREATED, "BLOCK2001", "성공적으로 사용자를 차단하였습니다."),
    SUCCESS_CANCEL_BLOCK(NO_CONTENT, "BLOCK2002", "성공적으로 사용자 차단을 취소하였습니다."),

    // Comment
    SUCCESS_UPLOAD_COMMENT(CREATED, "COMMENT2001", "성공적으로 댓글을 등록하였습니다."),
    SUCCESS_DELETE_COMMENT(NO_CONTENT, "COMMENT2002", "성공적으로 댓글을 삭제하였습니다."),

    // FCM
    SUCCESS_SAVE_TOKEN(CREATED, "FCM2001", "성공적으로 토큰을 등록하였습니다."),

    // Follow
    SUCCESS_FOLLOW(CREATED, "FOLLOW2001", "성공적으로 팔로우하였습니다."),
    SUCCESS_UNFOLLOW(NO_CONTENT, "FOLLOW2002", "성공적으로 팔로우를 취소하였습니다."),
    SUCCESS_DELETE_FOLLOWER(NO_CONTENT, "FOLLOW2003", "성공적으로 팔로워를 삭제하였습니다."),

    // Like
    SUCCESS_LIKE(CREATED, "LIKE2001", "성공적으로 좋아요를 처리하였습니다."),

    // Login
    SUCCESS_LOG_OUT(NO_CONTENT, "LOGIN2001", "성공적으로 로그아웃하였습니다"),
    SUCCESS_SIGN_OUT(NO_CONTENT, "LOGIN2002", "성공적으로 탈퇴하였습니다."),

    // Member
    VALID_HANDLE(NO_CONTENT, "MEMBER2001", "사용가능한 handle(아이디)입니다."),

    // Post
    SUCCESS_UPLOAD_POST(CREATED, "POST2001", "성공적으로 게시물을 등록하였습니다."),
    SUCCESS_DELETE_POST(NO_CONTENT, "POST2002", "성공적으로 게시물을 삭제하였습니다."),

    // Report
    SUCCESS_UPLOAD_REPORT(CREATED, "REPORT2001", "성공적으로 해당 게시물을 신고하였습니다."),

    // Tag
    SUCCESS_ACCEPT(CREATED, "TAG2001", "성공적으로 게시물 업로드 요청을 수락하였습니다."),
    SUCCESS_POST_ACCEPT(NO_CONTENT, "TAG2002", "성공적으로 게시물 업로드 요청을 수락하였습니다. 모든 요청이 수락되어 게시물이 업로드됩니다."),
    SUCCESS_REJECT(NO_CONTENT, "TAG2003", "성공적으로 게시물 업로드 요청을 거절하였습니다."),

    ;


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ReasonDTO getReason() {
        return ReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(true)
                .build();
    }

    @Override
    public ReasonDTO getReasonHttpStatus() {
        return ReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(true)
                .httpStatus(httpStatus)
                .build();
    }
}
