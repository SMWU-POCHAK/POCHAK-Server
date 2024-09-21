package com.apps.pochak.global.util;

import lombok.Data;
import org.springframework.data.domain.Page;

@Data
public class PageInfo {
    private Boolean lastPage;
    private int totalPages;
    private long totalElements;
    private int size;

    public <T> PageInfo(Page<T> page) {
        this.lastPage = page.isLast();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.size = page.getSize();
    }
}
