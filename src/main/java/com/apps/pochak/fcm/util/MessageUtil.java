package com.apps.pochak.fcm.util;

import com.apps.pochak.alarm.domain.Alarm;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;

import java.util.List;
import java.util.stream.Collectors;

public class MessageUtil {

    public static Message makeMessage(
            final Alarm alarm
    ) {
        return makeMessage(
                alarm.getReceiver().getFcmToken(),
                alarm.getPushNotificationTitle(),
                alarm.getPushNotificationBody(),
                alarm.getPushNotificationImage()
        );
    }

    public static MulticastMessage makeTagApprovalMessages(
            final List<Alarm> alarmList
    ) {
        Alarm alarm = alarmList.get(0);
        return makeMessages(
                alarmList.stream()
                        .filter(a -> a.getReceiver().hasFcmToken())
                        .map(a -> a.getReceiver().getFcmToken())
                        .collect(Collectors.toList()),
                alarm.getPushNotificationTitle(),
                alarm.getPushNotificationBody(),
                alarm.getPushNotificationImage()
        );
    }

    private static Message makeMessage(
            final String targetToken,
            final String title,
            final String body,
            final String image
    ) {
        Notification notification = Notification
                .builder()
                .setTitle(title)
                .setBody(body)
                .setImage(image)
                .build();

        return Message
                .builder()
                .setNotification(notification)
                .setToken(targetToken)
                .build();
    }

    private static MulticastMessage makeMessages(
            final List<String> targetTokens,
            final String title,
            final String body,
            final String image
    ) {
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .setImage(image)
                .build();

        return MulticastMessage.builder()
                .setNotification(notification)
                .addAllTokens(targetTokens)
                .build();

    }
}
