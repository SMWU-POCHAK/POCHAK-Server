package com.apps.pochak.alarm.dto.response;

import com.apps.pochak.alarm.domain.*;
import com.apps.pochak.alarm.dto.response.alarm_element.CommentAlarmElement;
import com.apps.pochak.alarm.dto.response.alarm_element.FollowAlarmElement;
import com.apps.pochak.alarm.dto.response.alarm_element.LikeAlarmElement;
import com.apps.pochak.alarm.dto.response.alarm_element.TagApprovalAlarmElement;
import com.apps.pochak.global.util.PageInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlarmElements {
    private PageInfo pageInfo;
    private List<AlarmElement> alarmList;

    public AlarmElements(final Page<Alarm> alarmPage) {
        this.pageInfo = new PageInfo(alarmPage);
        this.alarmList = alarmPage.stream().map(
                alarm -> {
                    if (alarm instanceof FollowAlarm) {
                        return new FollowAlarmElement((FollowAlarm) alarm);
                    } else if (alarm instanceof CommentAlarm) {
                        return new CommentAlarmElement((CommentAlarm) alarm);
                    } else if (alarm instanceof TagAlarm) {
                        return new TagApprovalAlarmElement((TagAlarm) alarm);
                    } else { // like alarm
                        return new LikeAlarmElement((LikeAlarm) alarm);
                    }
                }
        ).collect(Collectors.toList());
    }
}
