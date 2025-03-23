package pedroaba.java.race.utils;

import java.time.LocalDateTime;

public class Sleeper {
    public static void sleep(Long milliseconds) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime target = LocalDateTime.now().plusSeconds(milliseconds / 1000);
        while (now.isBefore(target)) {
            now = LocalDateTime.now();
        }
    }
}
