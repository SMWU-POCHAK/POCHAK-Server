package com.apps.pochak.alarm.dto.response;

import com.apps.pochak.alarm.domain.Alarm;
import com.apps.pochak.alarm.domain.AlarmType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public abstract class AlarmElement {
    private Long alarmId;
    private AlarmType alarmType;
    private Boolean isChecked;
    private LocalDateTime createdDate;

    protected AlarmElement(Alarm alarm) {
        this.alarmId = alarm.getId();
        this.alarmType = alarm.getAlarmType();
        this.isChecked = alarm.getIsChecked();
        this.createdDate = alarm.getCreatedDate();
    }
}
