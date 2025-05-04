package pedroaba.java.race.events;

import pedroaba.java.race.entities.Car;

import java.util.List;

public record PitStopCollisionEvent(
    Integer quantityOfCarOnPitStop,
    List<Car> cars
) {
}
