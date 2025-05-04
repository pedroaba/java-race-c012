package pedroaba.java.race.utils;

public class Sleeper {
    public static void sleep(Long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}