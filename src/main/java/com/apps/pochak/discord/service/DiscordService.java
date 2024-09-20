package com.apps.pochak.discord.service;

import com.apps.pochak.discord.client.DiscordClient;
import com.apps.pochak.discord.dto.DiscordMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.WebRequest;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class DiscordService {
    private final DiscordClient discordClient;
    private final Environment environment;

    @Value("${discord.webhook.channel-id}")
    private String discordChannelId;

    @Value("${discord.webhook.token}")
    private String discordToken;

    public void sendDiscordMessage(
            final Exception e,
            final WebRequest request
    ) {
        if (isDevServer()) {
            discordClient.sendAlarm(
                    discordChannelId,
                    discordToken,
                    DiscordMessage.getDevServerErrorMessage(e, request)
            );
        } else { // production server
            discordClient.sendAlarm(
                    discordChannelId,
                    discordToken,
                    DiscordMessage.getProdServerErrorMessage(e, request)
            );
        }
    }

    private boolean isDevServer() {
        return Arrays.asList(environment.getActiveProfiles()).contains("DEV");
    }
}
