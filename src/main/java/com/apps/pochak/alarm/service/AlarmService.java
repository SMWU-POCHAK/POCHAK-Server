package com.apps.pochak.alarm.service;

import com.apps.pochak.alarm.domain.Alarm;
import com.apps.pochak.alarm.domain.repository.AlarmRepository;
import com.apps.pochak.alarm.dto.response.AlarmElements;
import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.global.api_payload.code.BaseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.apps.pochak.global.api_payload.code.status.SuccessStatus.SUCCESS_CHECK_ALARM;

@Service
@Transactional
@RequiredArgsConstructor
public class AlarmService {
    private final AlarmRepository alarmRepository;

    @Transactional(readOnly = true)
    public AlarmElements getAllAlarms(
            final Accessor accessor,
            final Pageable pageable
    ) {
        final Page<Alarm> alarmPage = alarmRepository.getAllAlarm(accessor.getMemberId(), pageable);
        return new AlarmElements(alarmPage);
    }

    public BaseCode checkAlarm(
            final Accessor accessor,
            final Long alarmId
    ) {
        final Alarm alarm = alarmRepository.findAlarmById(alarmId, accessor.getMemberId());
        alarm.setIsChecked(true);
        return SUCCESS_CHECK_ALARM;
    }
}
