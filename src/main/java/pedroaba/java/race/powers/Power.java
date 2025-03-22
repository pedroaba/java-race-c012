package pedroaba.java.race.powers;

import pedroaba.java.race.entities.Car;

public abstract class Power {
    protected Car target;

    public Power(Car target) {
        this.target = target;
    }

    public abstract void activate();
}
