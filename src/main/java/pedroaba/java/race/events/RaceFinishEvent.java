package pedroaba.java.race.events;

import pedroaba.java.race.entities.Car;

public record RaceFinishEvent(Car car, Long finishTime) {
    public Car getCar() {
        return car;
    }

    public long getFinishTime() {
        return finishTime;
    }
}
