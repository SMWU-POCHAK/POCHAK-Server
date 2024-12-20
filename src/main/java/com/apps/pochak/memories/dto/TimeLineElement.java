package com.apps.pochak.memories.dto;

import com.apps.pochak.member.domain.Member;
import com.apps.pochak.memories.domain.MemoriesType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeLineElement {
    private MemoriesType memoriesType;
    private String postOwnerHandle;

    public static TimeLineElement of(final MemoriesType memoriesType, final Member postOwner) {
        return new TimeLineElement(memoriesType, postOwner.getHandle());
    }

    public static TimeLineElement of(final MemoriesType memoriesType) {
        return new TimeLineElement(memoriesType, null);
    }
}
