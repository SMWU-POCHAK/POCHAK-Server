package com.apps.pochak.discord.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.web.context.request.WebRequest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;

import static com.apps.pochak.global.util.RequestInfo.createRequestFullPath;

@Builder
@Getter
public class DiscordMessage {

    private String content;
    private List<Embed> embeds;

    public static DiscordMessage getDevServerErrorMessage(
            final Exception e,
            final WebRequest request
    ) {
        return DiscordMessage.builder()
                .content("# 😱 개발서버 에러 발생 😱")
                .embeds(
                        List.of(
                                Embed.builder()
                                        .title("<< 에러 정보 >>")
                                        .description(
                                                "### 🕖 발생 시간\n"
                                                        + LocalDateTime.now()
                                                        + "\n"
                                                        + "### 🔗 요청 URL\n"
                                                        + createRequestFullPath(request)
                                                        + "\n"
                                                        + "### 💬 Message\n"
                                                        + "`"
                                                        + e.getMessage()
                                                        + "`"
                                                        + "\n"
                                                        + "### 📄 Stack Trace\n"
                                                        + "```\n"
                                                        + getStackTraceToString(e).substring(0, 1000)
                                                        + "\n```"
                                        )
                                        .build()
                        )
                )
                .build();
    }

    public static DiscordMessage getProdServerErrorMessage(
            final Exception e,
            final WebRequest request
    ) {
        return DiscordMessage.builder()
                .content("# 🚨 운영서버 에러 발생 🚨")
                .embeds(
                        List.of(
                                Embed.builder()
                                        .title("<< 에러 정보 >>")
                                        .description(
                                                "### 🕖 발생 시간\n"
                                                        + LocalDateTime.now()
                                                        + "\n"
                                                        + "### 🔗 요청 URL\n"
                                                        + createRequestFullPath(request)
                                                        + "\n"
                                                        + "### 💬 Message\n"
                                                        + "`"
                                                        + e.getMessage()
                                                        + "`"
                                                        + "\n"
                                                        + "### 📄 Stack Trace\n"
                                                        + "```\n"
                                                        + getStackTraceToString(e).substring(0, 1000)
                                                        + "\n```"
                                        )
                                        .build()
                        )
                )
                .build();
    }

    private static String getStackTraceToString(
            final Exception e
    ) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}

@Builder
@Getter
class Embed {
    private String title;
    private String description;
}