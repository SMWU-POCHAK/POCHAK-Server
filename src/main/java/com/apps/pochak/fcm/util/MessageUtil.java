package com.apps.pochak.fcm.util;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;

import java.util.List;

public class MessageUtil {

    private static Message makeMessage(
            final String targetToken,
            final String title,
            final String body
    ) {
        Notification notification = Notification
                .builder()
                .setTitle(title)
                .setBody(body)
                .build();

        return Message
                .builder()
                .setNotification(notification)
                .setToken(targetToken)
                .build();
    }

    private static MulticastMessage makeMessages(
            final String title,
            final String body,
            final List<String> targetTokens
    ) {
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        return MulticastMessage.builder()
                .setNotification(notification)
                .addAllTokens(targetTokens)
                .build();

    }
}
