package com.apps.pochak.fcm.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

@Configuration
public class FCMConfig {

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        String fileResourceURL = "static/firebase-adminsdk.json";
        ClassPathResource resource = new ClassPathResource(fileResourceURL);

        FirebaseApp firebaseApp = null;
        List<FirebaseApp> firebaseApps = FirebaseApp.getApps();
        if (firebaseApps != null && !firebaseApps.isEmpty()) {
            for (FirebaseApp app : firebaseApps) {
                if (app.getName().equals(FirebaseApp.DEFAULT_APP_NAME)) {
                    firebaseApp = app;
                }
            }
        } else {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
                    .build();
            firebaseApp = FirebaseApp.initializeApp(options);
        }

        return firebaseApp;
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
