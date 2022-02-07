package no.nav.pto.veilarbfilter.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public static String fromLocalDateTimeToStr(LocalDateTime time) {
        return time.format(formatter);
    }

    public static LocalDateTime toLocalDateTimeOrNull(String date) {
        if (date == null) {
            return null;
        }
        return LocalDateTime.parse(date, formatter);
    }
}
