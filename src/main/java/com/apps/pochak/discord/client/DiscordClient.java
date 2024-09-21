package com.apps.pochak.discord.client;

import com.apps.pochak.discord.dto.DiscordMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "discord", url = "https://discord.com/api/webhooks")
@Qualifier("discord")
public interface DiscordClient {

    @PostMapping(value = "/{channelId}/{token}", consumes = MediaType.APPLICATION_JSON_VALUE)
    void sendAlarm(
            @PathVariable final String channelId,
            @PathVariable final String token,
            @RequestBody final DiscordMessage message
    );
}
