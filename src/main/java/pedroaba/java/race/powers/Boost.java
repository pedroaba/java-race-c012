package pedroaba.java.race.powers;

import pedroaba.java.race.entities.Car;
import pedroaba.java.race.utils.Sleeper;

public class Boost extends Power {
    private final double speedIncrease;
    private final long duration; // in milliseconds

    public Boost(Car target, double speedIncrease, long duration) {
        super(target);
        this.speedIncrease = speedIncrease;
        this.duration = duration;
    }

    @Override
    public void activate() {
        target.increaseSpeed(speedIncrease);
        Sleeper.sleep(duration);

        target.increaseSpeed(-speedIncrease);
    }
}
