package pedroaba.java.race;

import pedroaba.java.race.entities.Car;
import pedroaba.java.race.events.Dispatcher;

public class Lamborghini extends Car {
    private static final double DEFAULT_SPEED = 3.0;

    public Lamborghini(Dispatcher<Object> dispatcher) {
        super(Lamborghini.DEFAULT_SPEED, dispatcher);
    }
}
