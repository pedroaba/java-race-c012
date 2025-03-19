package pedroaba.java.race;

import pedroaba.java.race.domain.Car;

public class Ferrari extends Car {
    private static final double DEFAULT_SPEED = 1.3;
    private static final double DEFAULT_ACCELERATION = 1.2;

    public Ferrari() {
        super(Ferrari.DEFAULT_SPEED, Ferrari.DEFAULT_ACCELERATION);
    }
}
