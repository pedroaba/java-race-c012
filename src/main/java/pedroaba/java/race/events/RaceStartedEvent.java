package pedroaba.java.race.events;

public record RaceStartedEvent(Long startTime) {
    Long getStartTime() {
        return startTime;
    }
}
