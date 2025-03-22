package pedroaba.java.race.powers;

import pedroaba.java.race.entities.Car;

public class Banana extends Power {
    private long delay; // in milliseconds

    public Banana(Car target, long delay) {
        super(target);
        this.delay = delay;
    }

    @Override
    public void activate() {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
