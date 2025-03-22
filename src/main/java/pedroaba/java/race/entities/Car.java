package pedroaba.java.race.entities;

import pedroaba.java.race.enums.GameEventName;
import pedroaba.java.race.events.Dispatcher;
import pedroaba.java.race.powers.Power;

public abstract class Car {
    private final Dispatcher<Object> dispatcher;
    private double speed;
    private Power power;

    public Car(double speed, Dispatcher<Object> dispatcher) {
        if (speed < 0) {
            throw new IllegalArgumentException("Speed/Acceleration must be greater than 0");
        }
        this.speed = speed;
        this.dispatcher = dispatcher;
    }

    public void move(double currentPosition) {
        double newPosition = currentPosition;
        if (currentPosition > 0) {
            newPosition += speed;
        }
        dispatcher.emmit(GameEventName.RUNNING, newPosition);
    }

    public void increaseSpeed(double value) {
        this.speed += value;
    }

    public void setPower(Power power) {
        this.power = power;
    }

    public void usePower() {
        if (power != null) {
            new Thread(() -> power.activate()).start();
        }
    }
}
