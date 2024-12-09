package com.apps.pochak.alarm.service;

import com.apps.pochak.alarm.domain.repository.AlarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Component
@RequiredArgsConstructor
public class AlarmDeletionScheduler {

    private final AlarmRepository alarmRepository;

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void deleteExpiredAlarms() {
        LocalDateTime expirationDate = LocalDateTime.now().minusDays(30);
        alarmRepository.deleteExpiredAlarms(expirationDate);
    }

}
