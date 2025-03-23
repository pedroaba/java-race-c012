package pedroaba.java.race.events;

public record AllCarFinishEvent(Long finishTime) {
    public long getFinishTime() {
        return finishTime;
    }
}
