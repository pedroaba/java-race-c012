package pedroaba.java.race.utils;

public class Sleeper {
    public static void sleep(Long milliseconds) {
        try {
            // Mudei para Thread.sleep() porque o outro modo ficava usando cpu enquanto dava sleep :v
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}