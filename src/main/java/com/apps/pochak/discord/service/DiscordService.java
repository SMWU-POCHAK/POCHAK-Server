package com.apps.pochak.discord.service;

import com.apps.pochak.discord.client.DiscordClient;
import com.apps.pochak.discord.dto.DiscordMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.WebRequest;

@Service
@RequiredArgsConstructor
public class DiscordService {
    private final DiscordClient discordClient;

    @Value("${discord.webhook.channel-id}")
    private String discordChannelId;

    @Value("${discord.webhook.token}")
    private String discordToken;

    public void sendDiscordMessage(
            final Exception e,
            final WebRequest request
    ) {
        discordClient.sendAlarm(
                discordChannelId,
                discordToken,
                DiscordMessage.getDevServerErrorMessage(e, request)
        );
    }
}
