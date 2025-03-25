package pedroaba.java.race.powers;

import pedroaba.java.race.entities.Car;

public class Boost extends Power {
    private final double speedIncrease;

    public Boost(Car target, double speedIncrease) {
        super(target);
        this.speedIncrease = speedIncrease;
    }

    @Override
    public void activate() {
        target.increaseSpeed(speedIncrease);
    }
}
