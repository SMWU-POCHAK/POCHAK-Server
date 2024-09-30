package com.apps.pochak.global.util;

import lombok.Data;
import org.springframework.data.domain.Page;

public class PageUtil {
    public static <T>T getFirstContentFromPage(final Page<T> page) {
        if (page.hasContent()) {
            return page.getContent().get(0);
        } else return null;
    }
}
