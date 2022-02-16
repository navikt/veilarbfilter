package no.nav.pto.veilarbfilter.util;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    private static DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static LocalDateTime toLocalDateTimeOrNull(String date) {
        if (date == null) {
            return null;
        }
        return LocalDateTime.parse(date, format);
    }

    public static Timestamp fromLocalDateTimeToTimestamp(LocalDateTime dateTime) {
        return Timestamp.valueOf(dateTime.format(format));
    }

    public static LocalDateTime fromTimestampToLocalDateTime(Timestamp timestamp) {
        return timestamp.toLocalDateTime();
    }
}
