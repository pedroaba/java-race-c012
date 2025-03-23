package pedroaba.java.race.powers;

import pedroaba.java.race.entities.Car;
import pedroaba.java.race.utils.Sleeper;

public class RedShell extends Power {
    private final double speedDecrease;
    private final long duration; // in milliseconds

    public RedShell(Car target, double speedDecrease, long duration) {
        super(target);
        this.speedDecrease = speedDecrease;
        this.duration = duration;
    }

    @Override
    public void activate() {
        target.increaseSpeed(-speedDecrease);
        Sleeper.sleep(duration);

        target.increaseSpeed(speedDecrease);
    }
}
