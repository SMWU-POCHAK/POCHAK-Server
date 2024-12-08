package com.apps.pochak.global.util;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TimeUtil {
    public static LocalDateTime atMidnight(int year) {
        return LocalDate.now().minusYears(year).atStartOfDay();
    }
}
