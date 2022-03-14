package no.nav.pto.veilarbfilter.util;

import lombok.Getter;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    @Getter
    public static DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Timestamp fromLocalDateTimeToTimestamp(LocalDateTime dateTime) {
        return Timestamp.valueOf(dateTime.format(format));
    }

    public static LocalDateTime fromTimestampToLocalDateTime(Timestamp timestamp) {
        return timestamp.toLocalDateTime();
    }
}
