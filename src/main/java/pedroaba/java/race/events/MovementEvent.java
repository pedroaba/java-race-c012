package pedroaba.java.race.events;

import org.jetbrains.annotations.NotNull;
import pedroaba.java.race.entities.Car;
import pedroaba.java.race.enums.GameEventName;

public record MovementEvent(Car car, GameEventName eventName, Double speed, Double position) {
    @Override
    public @NotNull String toString() {
        return "Car %s: speed: %s km/h - position: %s m".formatted(car, speed, position);
    }
}
