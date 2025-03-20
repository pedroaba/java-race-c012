package pedroaba.java.race;

import pedroaba.java.race.entities.Car;
import pedroaba.java.race.events.Dispatcher;

public class Beetle extends Car {
    private static final double DEFAULT_SPEED = 1.0;

    public Beetle(Dispatcher<Object> dispatcher) {
        super(Beetle.DEFAULT_SPEED, dispatcher);
    }
}
