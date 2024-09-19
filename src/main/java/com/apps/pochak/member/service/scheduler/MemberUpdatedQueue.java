package com.apps.pochak.member.service.scheduler;

import com.apps.pochak.alarm.service.Scheduler.AlarmProfileUpdateScheduler;
import com.apps.pochak.member.domain.Member;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class MemberUpdatedQueue {
    private final ConcurrentLinkedQueue<Member> membersToUpdate = new ConcurrentLinkedQueue<>();
    private final AlarmProfileUpdateScheduler alarmProfileUpdateScheduler;

    public MemberUpdatedQueue(AlarmProfileUpdateScheduler alarmProfileUpdateScheduler) {
        this.alarmProfileUpdateScheduler = alarmProfileUpdateScheduler;
    }

    public void add(Member member) {
        membersToUpdate.add(member);
    }

    public List<Member> getMembersUpdated() {
        return List.copyOf(membersToUpdate);
    }

    public void clearProcessedMembers(List<Member> processedMembers) {
        membersToUpdate.removeAll(processedMembers);
    }

    public void updateMemberInfoForMembersInQueue() {
        for (Member member : getMembersUpdated()) {
            try {
                alarmProfileUpdateScheduler.updateAlarmsForMember(member);
            } catch (Exception e) {
                add(member);
            }
        }

        clearProcessedMembers(getMembersUpdated());
    }

    public void profileUpdateQueueWhenServerShutDown() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            updateMemberInfoForMembersInQueue();
            membersToUpdate.clear();
        }));
    }
}
