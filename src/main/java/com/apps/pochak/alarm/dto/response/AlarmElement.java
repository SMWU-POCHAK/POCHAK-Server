package com.apps.pochak.alarm.dto.response;

import com.apps.pochak.alarm.domain.Alarm;
import com.apps.pochak.alarm.domain.AlarmType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AlarmElement {
    private Long alarmId;
    private AlarmType alarmType;
    private Boolean isChecked;

    protected AlarmElement (Alarm alarm) {
        this.alarmId = alarm.getId();
        this.alarmType = alarm.getAlarmType();
        this.isChecked = alarm.getIsChecked();
    }
}
