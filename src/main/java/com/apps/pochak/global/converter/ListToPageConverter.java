package com.apps.pochak.global.converter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static com.apps.pochak.global.Constant.DEFAULT_PAGING_SIZE;

public class ListToPageConverter {
    public static <T> Page<T> toPage(List<T> list, PageRequest pageRequest) {
        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());

        return new PageImpl<>(
                list.subList(start, end),
                pageRequest,
                list.size()
        );
    }

    public static <T> Page<T> toPage(List<T> list) {
        PageRequest pageRequest = PageRequest.of(0, DEFAULT_PAGING_SIZE);
        return toPage(list, pageRequest);
    }
}
