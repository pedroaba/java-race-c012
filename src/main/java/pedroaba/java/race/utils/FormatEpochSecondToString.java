package pedroaba.java.race.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class FormatEpochSecondToString {
    public static String formatEpochSecond(long epochSecond) {
        final Instant timestamp = Instant.ofEpochSecond(epochSecond);
        final ZonedDateTime zone = timestamp.atZone(ZoneId.systemDefault());

        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return zone.format(formatter);
    }
}
