package com.apps.pochak.alarm.service;

import com.apps.pochak.alarm.domain.Alarm;
import com.apps.pochak.alarm.domain.TagAlarm;
import com.apps.pochak.alarm.domain.repository.AlarmRepository;
import com.apps.pochak.fcm.service.FCMService;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.tag.domain.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagAlarmService {
    private final AlarmRepository alarmRepository;
    private final FCMService fcmService;

    public void saveTagApprovalAlarms(
            final List<Tag> tagList,
            final Member tagger
    ) {
        final List<Alarm> tagApprovalAlarmList = tagList.stream().map(
                tag -> new TagAlarm(tag, tagger, tag.getMember())
        ).collect(Collectors.toList());
        alarmRepository.saveAll(tagApprovalAlarmList);
        fcmService.sendPushNotification(tagApprovalAlarmList);
    }

    public void deleteAlarmByTag(final Tag tag) {
        alarmRepository.deleteAlarmByTag(tag.getId());
    }

    public void deleteAlarmByTagList(final List<Tag> tagList) {
        List<Long> tagIdList = tagList.stream().map(Tag::getId).collect(Collectors.toList());
        alarmRepository.deleteAlarmByTagIdList(tagIdList);
    }
}
