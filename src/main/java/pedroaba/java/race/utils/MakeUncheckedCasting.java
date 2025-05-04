package pedroaba.java.race.utils;

public class MakeUncheckedCasting {
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object o) {
        return (T) o;
    }
}
