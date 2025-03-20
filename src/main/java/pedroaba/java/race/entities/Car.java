package pedroaba.java.race.entities;

import pedroaba.java.race.enums.GameEventName;
import pedroaba.java.race.events.Dispatcher;

public abstract class Car {
    private final Dispatcher<Object> dispatcher;
    private double speed;

    public Car(double speed, Dispatcher<Object> dispatcher) {
        if (speed < 0) {
            throw new IllegalArgumentException("Speed/Acceleration must be greater than 0");
        }

        this.speed = speed;

        // Event bus controller
        this.dispatcher = dispatcher;
    }

    public void move(double currentPosition) {
        double newPosition = currentPosition;
        if (currentPosition > 0) {
            newPosition += speed;
        }

        dispatcher.emmit(GameEventName.RUNNING, newPosition);
    }
}
