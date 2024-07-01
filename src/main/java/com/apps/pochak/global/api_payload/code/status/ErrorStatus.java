package com.apps.pochak.global.api_payload.code.status;

import com.apps.pochak.global.api_payload.code.BaseErrorCode;
import com.apps.pochak.global.api_payload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    // 가장 일반적인 응답
    _INTERNAL_SERVER_ERROR(INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    _UNAUTHORIZED(UNAUTHORIZED, "COMMON401", "인증이 필요합니다. 권한을 확인해주세요."),
    _FORBIDDEN(FORBIDDEN, "COMMON403", "금지된 요청입니다."),

    // Global
    IO_EXCEPTION(INTERNAL_SERVER_ERROR, "COMMON5001", "서버 IO Exception 발생, 관리자에게 문의 바랍니다"),

    // Alarm
    INVALID_ALARM_ID(BAD_REQUEST, "ALARM4001", "유효하지 않은 알람 아이디입니다."),
    NOT_YOUR_ALARM(UNAUTHORIZED, "ALARM4002", "해당 알람의 확인 권한이 없습니다."),
    CANNOT_PREVIEW(BAD_REQUEST, "ALARM4003", "해당 알람은 미리보기 할 수 없는 알람입니다."),

    // Block
    BLOCK_ONESELF(BAD_REQUEST, "BLOCK4001", "자기 자신을 차단할 수 없습니다."),

    // Comment
    INVALID_COMMENT_ID(BAD_REQUEST, "COMMENT4001", "유효하지 않은 댓글 아이디입니다."),

    // Follow
    NOT_FOLLOW(INTERNAL_SERVER_ERROR, "FOLLOW4001", "데이터에러: 팔로우 상태를 찾을 수 없습니다, 관리자에게 문의 바랍니다."),
    FOLLOW_ONESELF(BAD_REQUEST, "FOLLOW4002", "자기 자신을 팔로우할 수 없습니다."),

    // Like

    // Login
    INVALID_ACCESS_TOKEN(BAD_REQUEST, "LOGIN4001", "잘못된 엑세스 토큰입니다."),
    INVALID_REFRESH_TOKEN(BAD_REQUEST, "LOGIN4002", "잘못된 리프레시 토큰입니다."),
    INVALID_TOKEN_SIGNATURE(BAD_REQUEST, "LOGIN4003", "잘못된 토큰 서명입니다."),
    UNSUPPORTED_TOKEN(BAD_REQUEST, "LOGIN4004", "지원하지 않는 형식의 토큰입니다."),
    MALFORMED_TOKEN(BAD_REQUEST, "LOGIN4005", "유효하지 않은 구성의 토큰입니다."),
    NULL_TOKEN(BAD_REQUEST, "LOGIN4006", "토큰이 존재하지 않습니다."),
    EXIST_USER(BAD_REQUEST, "LOGIN4007", "존재하는 유저입니다."),
    NULL_REFRESH_TOKEN(BAD_REQUEST, "LOGIN4008", "리프레시 토큰이 존재하지 않습니다."),
    EXPIRED_ACCESS_TOKEN(BAD_REQUEST, "LOGIN4009", "만료된 액세스 토큰입니다."),
    EXPIRED_REFRESH_TOKEN(BAD_REQUEST, "LOGIN4010", "만료된 리프레시 토큰입니다."),
    INVALID_PUBLIC_KEY(BAD_REQUEST, "LOGIN4011", "공개키를 가져올 수 없습니다."),
    INVALID_USER_INFO(BAD_REQUEST, "LOGIN4012", "유저 정보를 가져올 수 없습니다."),
    INVALID_OAUTH_TOKEN(BAD_REQUEST, "LOGIN4013", "토큰을 가져올 수 없습니다."),
    FAIL_VALIDATE_TOKEN(BAD_REQUEST, "LOGIN4013", "토큰 유효성 검사 중 오류가 발생했습니다."),

    // Member
    INVALID_MEMBER_ID(BAD_REQUEST, "MEMBER4001", "유효하지 않은 멤버의 아이디입니다."),
    INVALID_MEMBER_HANDLE(BAD_REQUEST, "MEMBER4002", "유효하지 않은 멤버의 handle입니다."),
    DUPLICATE_HANDLE(BAD_REQUEST, "MEMBER4002", "중복되는 handle(아이디) 입니다."),
    UNAUTHORIZED_MEMBER_REQUEST(FORBIDDEN, "MEMBER4003", "프로필을 수정할 권한이 없습니다."),

    // Post
    INVALID_POST_ID(BAD_REQUEST, "POST4001", "유효하지 않은 게시물 아이디입니다."),
    NOT_YOUR_POST(UNAUTHORIZED, "POST4002", "해당 게시물의 삭제 권한이 없습니다."),
    PRIVATE_POST(UNAUTHORIZED, "POST4003", "공개되지 않은 게시물입니다."),

    // Tag
    INVALID_TAG_ID(BAD_REQUEST, "TAG4001", "유효하지 않은 태그 아이디입니다."),
    NOT_MY_TAG(UNAUTHORIZED, "TAG4002", "해당 태그의 수락 여부를 변경할 권한이 없습니다"),

    // Image
    DELETE_FILE_ERROR(SERVICE_UNAVAILABLE, "IMAGE501", "파일 삭제를 실패하였습니다."),
    S3_UPLOAD_ERROR(SERVICE_UNAVAILABLE, "IMAGE502", "S3 업로드를 실패하였습니다."),
    CONVERT_FILE_ERROR(SERVICE_UNAVAILABLE, "IMAGE503", "MultipartFile을 File로 전환 실패하였습니다."),
    NULL_FILE(BAD_REQUEST, "IMAGE504", "파일이 존재하지 않습니다.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build();
    }
}
