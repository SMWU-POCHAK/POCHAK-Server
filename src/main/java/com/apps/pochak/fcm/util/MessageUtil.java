package com.apps.pochak.fcm.util;

import com.apps.pochak.alarm.domain.Alarm;
import com.google.firebase.messaging.*;

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

    public static MulticastMessage makeMessages(
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

        return Message.builder()
                .setNotification(notification)
                .setApnsConfig(iOSConfig())
                .setAndroidConfig(androidConfig())
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
                .setApnsConfig(iOSConfig())
                .setAndroidConfig(androidConfig())
                .addAllTokens(targetTokens)
                .build();
    }

    private static ApnsConfig iOSConfig() {
        Aps aps = Aps.builder()
                .setSound("default")
                .build();

        return ApnsConfig.builder()
                .setAps(aps)
                .build();
    }

    private static AndroidConfig androidConfig() {
        AndroidNotification androidNotification = AndroidNotification.builder()
                .setSound("default")
                .build();

        return AndroidConfig.builder()
                .setNotification(androidNotification)
                .build();
    }
}
