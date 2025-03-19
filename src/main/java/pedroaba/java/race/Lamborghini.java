package pedroaba.java.race;

import pedroaba.java.race.domain.Car;

public class Lamborghini extends Car {
    private static final double DEFAULT_SPEED = 3.0;
    private static final double DEFAULT_ACCELERATION = 1.4;

    public Lamborghini() {
        super(Lamborghini.DEFAULT_SPEED, Lamborghini.DEFAULT_ACCELERATION);
    }
}
