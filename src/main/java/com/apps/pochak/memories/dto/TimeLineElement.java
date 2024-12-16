package com.apps.pochak.memories.dto;

import com.apps.pochak.memories.domain.MemoriesType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeLineElement {
    private MemoriesType memoriesType;
    private String postOwner;

    public static TimeLineElement from(final MemoriesType memoriesType, final String postOwner) {
        return new TimeLineElement(memoriesType, postOwner);
    }

    public static TimeLineElement from(final MemoriesType memoriesType) {
        return new TimeLineElement(memoriesType, null);
    }
}
