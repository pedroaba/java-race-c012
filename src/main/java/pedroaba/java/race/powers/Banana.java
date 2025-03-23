package pedroaba.java.race.powers;

import pedroaba.java.race.entities.Car;
import pedroaba.java.race.utils.Sleeper;

public class Banana extends Power {
    private final long delay; // in milliseconds

    public Banana(Car target, long delay) {
        super(target);
        this.delay = delay;
    }

    @Override
    public void activate() {
        Sleeper.sleep(delay);
    }
}
