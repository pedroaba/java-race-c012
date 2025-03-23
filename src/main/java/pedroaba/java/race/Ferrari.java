package pedroaba.java.race;

import pedroaba.java.race.entities.Car;
import pedroaba.java.race.events.Dispatcher;

import java.util.function.Consumer;

public class Ferrari extends Car {
    private static final double DEFAULT_SPEED = 1.3;

    public Ferrari(Dispatcher<Object> dispatcher, int trackLength, Consumer<Object> consumer) {
        super(Ferrari.DEFAULT_SPEED, dispatcher, trackLength, consumer);
    }
}
