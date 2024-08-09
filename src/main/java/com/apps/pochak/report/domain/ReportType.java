package com.apps.pochak.report.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReportType {

    NOT_INTERESTED("마음에 들지 않습니다."),
    SPAM("스팸"),
    NUDITY_OR_SEXUAL_CONTENT("나체 이미지 또는 성적 행위"),
    FRAUD_OR_SCAM("사기 또는 거짓"),
    HATE_SPEECH_OR_SYMBOL("혐오 발언 또는 상징"),
    MISINFORMATION("거짓 정보")
    ;

    private final String message;
}
