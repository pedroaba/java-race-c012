package pedroaba.java.race;

import pedroaba.java.race.domain.Car;

public class Beetle extends Car {
    private static final double DEFAULT_SPEED = 1.0;
    private static final double DEFAULT_ACCELERATION = 1.0;

    public Beetle() {
        super(Beetle.DEFAULT_SPEED, Beetle.DEFAULT_ACCELERATION);
    }
}
