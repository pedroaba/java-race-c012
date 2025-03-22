package pedroaba.java.race.entities;

import pedroaba.java.race.constants.Config;
import pedroaba.java.race.enums.GameEventName;
import pedroaba.java.race.events.Dispatcher;
import pedroaba.java.race.powers.Power;
import pedroaba.java.race.events.MovementEvent;
import pedroaba.java.race.events.RaceFinishEvent;

import java.time.LocalDateTime;
import java.time.ZoneId;

public abstract class Car extends Thread {
    private final Dispatcher<Object> dispatcher;
    private Power power;
    private double speed;

    private final Integer trackLength;

    public Car(double speed, Dispatcher<Object> dispatcher, Integer trackLength) {
        if (speed <= 0) {
            throw new IllegalArgumentException("Speed must be greater than 0");
        }

        this.trackLength = trackLength;
        this.speed = speed;
        this.dispatcher = dispatcher;
    }

    public double getSpeed() {
        return speed;
    }

    public Dispatcher<Object> getDispatcher() {
        return dispatcher;
    }

    // Método que simula o movimento e dispara eventos
    public void move(double currentPosition) {
        MovementEvent movementEvent = new MovementEvent(this, GameEventName.RUNNING, this.speed, (int) currentPosition);

        dispatcher.emmit(GameEventName.RUNNING, movementEvent);
    }

    @Override
    public void run() {
        double position = 0;
        while (position < trackLength) {
            position += getSpeed();
            move(position);

            try {
                Thread.sleep(Config.TIME_BETWEEN_EACH_MOVEMENT);
            } catch (InterruptedException e) {
                e.fillInStackTrace();
            }
        }
        dispatcher.emmit(GameEventName.RUNNING, newPosition);

        LocalDateTime now = LocalDateTime.now();
        ZoneId zoneId = ZoneId.systemDefault();
        long epochSeconds = now.atZone(zoneId).toEpochSecond();

        RaceFinishEvent raceFinishEvent = new RaceFinishEvent(this, epochSeconds);
        getDispatcher().emmit(GameEventName.FINISHED, raceFinishEvent);
    }

    @Override
    public String toString() {
        return "[CarThreadId: %d | Car Type: %s]".formatted(Thread.currentThread().threadId(), this.getClass().getSimpleName());
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
