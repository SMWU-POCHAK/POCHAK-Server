package com.apps.pochak.fcm.util;

import com.apps.pochak.alarm.domain.Alarm;
import com.google.firebase.messaging.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MessageUtil {

    public static Message makeMessage(
            final Alarm alarm
    ) {
        return makeMessage(
                alarm.getReceiver().getFcmToken(),
                alarm
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
                alarm
        );
    }

    private static Message makeMessage(
            final String targetToken,
            final Alarm alarm
    ) {
        Notification notification = Notification
                .builder()
                .setTitle(alarm.getPushNotificationTitle())
                .setBody(alarm.getPushNotificationBody())
                .setImage(alarm.getPushNotificationImage())
                .build();

        return Message.builder()
                .setNotification(notification)
                .setApnsConfig(iOSConfig())
                .setAndroidConfig(androidConfig(alarm))
                .setToken(targetToken)
                .build();
    }

    private static MulticastMessage makeMessages(
            final List<String> targetTokens,
            final Alarm alarm
    ) {
        Notification notification = Notification.builder()
                .setTitle(alarm.getPushNotificationTitle())
                .setBody(alarm.getPushNotificationBody())
                .setImage(alarm.getPushNotificationImage())
                .build();

        return MulticastMessage.builder()
                .setNotification(notification)
                .setApnsConfig(iOSConfig())
                .setAndroidConfig(androidConfig(alarm))
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

    private static AndroidConfig androidConfig(final Alarm alarm) {
        AndroidNotification androidNotification = AndroidNotification.builder()
                .setSound("default")
                .build();

        return AndroidConfig.builder()
                .setNotification(androidNotification)
                .putAllData(
                        androidDataPayload(
                                alarm.getPushNotificationTitle(),
                                alarm.getPushNotificationBody(),
                                alarm.getPushNotificationImage()
                        ))
                .build();
    }

    private static Map<String, String> androidDataPayload(
            final String title,
            final String body,
            final String image
    ) {
        Map<String, String> data = new HashMap<>();
        data.put("title", title);
        data.put("body", body);
        data.put("image", image);
        return data;
    }
}
