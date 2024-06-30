package com.apps.pochak.alarm.service;

import com.apps.pochak.alarm.domain.Alarm;
import com.apps.pochak.alarm.domain.repository.AlarmRepository;
import com.apps.pochak.alarm.dto.response.AlarmElements;
import com.apps.pochak.global.api_payload.code.BaseCode;
import com.apps.pochak.login.jwt.JwtService;
import com.apps.pochak.member.domain.Member;
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
    private final JwtService jwtService;

    public AlarmElements getAllAlarms(Pageable pageable) {
        final Member loginMember = jwtService.getLoginMember();
        final Page<Alarm> alarmPage = alarmRepository.getAllAlarm(loginMember.getId(), pageable);
        return new AlarmElements(alarmPage);
    }

    public BaseCode checkAlarm(Long alarmId) {
        final Member loginMember = jwtService.getLoginMember();
        final Alarm alarm = alarmRepository.findAlarmById(alarmId, loginMember);
        alarm.setIsChecked(true);
        return SUCCESS_CHECK_ALARM;
    }
}
