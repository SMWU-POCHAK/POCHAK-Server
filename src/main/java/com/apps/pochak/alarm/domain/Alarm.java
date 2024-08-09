package com.apps.pochak.alarm.domain;

import com.apps.pochak.global.BaseEntity;
import com.apps.pochak.member.domain.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.SQLRestriction;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@DynamicInsert
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn
@SQLRestriction("status = 'ACTIVE'")
@NoArgsConstructor(access = PROTECTED)
public abstract class Alarm extends BaseEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "receiver_id")
    private Member receiver;

    @Setter
    @Column(columnDefinition = "boolean default false")
    private Boolean isChecked;

    @Enumerated(EnumType.STRING)
    private AlarmType alarmType;

    protected Alarm(
            final Member receiver, final AlarmType alarmType
    ) {
        this.receiver = receiver;
        this.alarmType = alarmType;
    }
}
