package pedroaba.java.race.powers;

import pedroaba.java.race.entities.Car;

public class Boost extends Power {
    private int speedIncrease;
    private long duration; // in milliseconds

    public Boost(Car target, int speedIncrease, long duration) {
        super(target);
        this.speedIncrease = speedIncrease;
        this.duration = duration;
    }

    @Override
    public void activate() {
        target.increaseSpeed(speedIncrease);
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        target.increaseSpeed(-speedIncrease); // Reset speed after boost
    }
}
