package pedroaba.java.race.powers;

import pedroaba.java.race.entities.Car;
import pedroaba.java.race.utils.Sleeper;

public class RedShell extends Power {
    private final double speedDecrease;
    private final long duration; // (ms)

    public RedShell(Car target, double speedDecrease, long duration) {
        super(target);
        this.speedDecrease = speedDecrease;
        this.duration = duration;
    }

    @Override
    public void activate() {
        // Calcula a diminuição real p/ ficar a cima de 1.5
        double actualDecrease = Math.min(speedDecrease, target.getSpeed() - 1.5);

        target.increaseSpeed(-actualDecrease);
        Sleeper.sleep(duration);

        target.increaseSpeed(actualDecrease);
    }
}