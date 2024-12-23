package com.apps.pochak.global;

import com.apps.pochak.alarm.domain.Alarm;
import com.apps.pochak.fcm.service.FCMService;
import com.apps.pochak.global.image.CloudStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;


public abstract class ServiceTest {

    @MockBean
    FCMService fcmService;

    @MockBean
    CloudStorageService cloudStorageService;

    @BeforeEach
    void setUp() {
        when(cloudStorageService.upload(any(), any())).thenReturn("");
        doNothing().when(fcmService).sendPushNotification((Alarm) any());
        doNothing().when(fcmService).sendPushNotification((List<Alarm>) any());
    }
}
