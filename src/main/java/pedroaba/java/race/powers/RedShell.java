package pedroaba.java.race.powers;

import pedroaba.java.race.entities.Car;

public class RedShell extends Power {
    private int speedDecrease;
    private long duration; // in milliseconds

    public RedShell(Car target, int speedDecrease, long duration) {
        super(target);
        this.speedDecrease = speedDecrease;
        this.duration = duration;
    }

    @Override
    public void activate() {
        target.increaseSpeed(-speedDecrease);
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        target.increaseSpeed(speedDecrease); // Reset speed after effect
    }
}
