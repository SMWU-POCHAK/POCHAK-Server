package com.apps.pochak.alarm.service.Scheduler;

import com.apps.pochak.alarm.domain.*;
import com.apps.pochak.alarm.domain.repository.AlarmRepository;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@EnableScheduling
public class AlarmProfileUpdateScheduler {
    private final AlarmRepository alarmRepository;
    private final MemberRepository memberRepository;

    public AlarmProfileUpdateScheduler(AlarmRepository alarmRepository, MemberRepository memberRepository) {
        this.alarmRepository = alarmRepository;
        this.memberRepository = memberRepository;
    }

    @Scheduled(fixedRate = 300000)  // 5 minutes
    public void updateProfileInfoInAlarms() {
        LocalDateTime recentTime = LocalDateTime.now().minusMinutes(10);
        List<Member> recentProfileUpdates = memberRepository.findRecentProfileUpdates(recentTime);

        for (Member member : recentProfileUpdates) {
            updateAlarmsForMember(member);
        }
    }

    private void updateAlarmsForMember(Member member) {
        List<CommentAlarm> commentAlarms = alarmRepository.findCommentAlarmsByWriterId(member.getId());
        List<TagAlarm> tagAlarms = alarmRepository.findTagAlarmsByTaggerId(member.getId());
        List<LikeAlarm> likeAlarms = alarmRepository.findLikeAlarmsByLikeMemberId(member.getId());
        List<FollowAlarm> followAlarms = alarmRepository.findFollowAlarmsBySenderId(member.getId());

        updateAlarmsProfileInfo(commentAlarms, member);
        updateAlarmsProfileInfo(tagAlarms, member);
        updateAlarmsProfileInfo(likeAlarms, member);
        updateAlarmsProfileInfo(followAlarms, member);
    }

    private <T extends Alarm> void updateAlarmsProfileInfo(List<T> alarmsToUpdate, Member updatedMember) {
        for (Alarm alarm : alarmsToUpdate) {
            if (alarm instanceof CommentAlarm) {
                CommentAlarm commentAlarm = (CommentAlarm) alarm;
                boolean isNameUpdated = !commentAlarm.getWriterName().equals(updatedMember.getName());
                boolean isProfileImageUpdated = !commentAlarm.getWriterProfileImage().equals(updatedMember.getProfileImage());

                if (isNameUpdated || isProfileImageUpdated) {
                    commentAlarm.updateWriterInfo(updatedMember);
                    alarmRepository.save(commentAlarm);
                }

            } else if (alarm instanceof TagAlarm) {
                TagAlarm tagAlarm = (TagAlarm) alarm;
                boolean isNameUpdated = !tagAlarm.getTaggerName().equals(updatedMember.getName());
                boolean isProfileImageUpdated = !tagAlarm.getTaggerProfileImage().equals(updatedMember.getProfileImage());

                if (isNameUpdated || isProfileImageUpdated) {
                    tagAlarm.updateWriterInfo(updatedMember);
                    alarmRepository.save(tagAlarm);
                }

            } else if (alarm instanceof LikeAlarm) {
                LikeAlarm likeAlarm = (LikeAlarm) alarm;
                boolean isNameUpdated = !likeAlarm.getLikeMemberName().equals(updatedMember.getName());
                boolean isProfileImageUpdated = !likeAlarm.getLikeMemberProfileImage().equals(updatedMember.getProfileImage());

                if (isNameUpdated || isProfileImageUpdated) {
                    likeAlarm.updateWriterInfo(updatedMember);
                    alarmRepository.save(likeAlarm);
                }

            } else if (alarm instanceof FollowAlarm) {
                FollowAlarm followAlarm = (FollowAlarm) alarm;
                boolean isNameUpdated = !followAlarm.getSenderName().equals(updatedMember.getName());
                boolean isProfileImageUpdated = !followAlarm.getSenderProfileImage().equals(updatedMember.getProfileImage());

                if (isNameUpdated || isProfileImageUpdated) {
                    followAlarm.updateWriterInfo(updatedMember);
                    alarmRepository.save(followAlarm);
                }
            }
        }
    }


}
