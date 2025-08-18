package com.example.bookingsystem.utils;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {
    private static final ZoneId LONDON_ZONE = ZoneId.of("Europe/London");

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm z");

    public static String format(OffsetDateTime dateTime) {
        return dateTime.atZoneSameInstant(LONDON_ZONE).format(FORMATTER);
    }
}
