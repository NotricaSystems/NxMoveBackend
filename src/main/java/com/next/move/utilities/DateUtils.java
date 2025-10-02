package com.next.move.utilities;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class DateUtils {

    public static boolean isOlderThanXDays(Instant date, int x) {
        Instant now = Instant.now();
        Instant eightDaysAgo = now.minus(x, ChronoUnit.DAYS);
        return date.isBefore(eightDaysAgo);
    }
}
