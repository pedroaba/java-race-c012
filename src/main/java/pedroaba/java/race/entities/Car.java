package pedroaba.java.race.entities;

import org.jetbrains.annotations.Nullable;
import pedroaba.java.race.constants.Config;
import pedroaba.java.race.enums.GameEventName;
import pedroaba.java.race.events.Dispatcher;
import pedroaba.java.race.events.MovementEvent;
import pedroaba.java.race.events.RaceFinishEvent;
import pedroaba.java.race.powers.Power;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.function.Consumer;

public abstract class Car extends Thread {
    private final Dispatcher<Object> dispatcher;
    private final Integer trackLength;
    private final Consumer<Object> dispatchToApplyPower;
    private Power power;
    private double speed;
    private Boolean finishRace = false;

    public Car(double speed, Dispatcher<Object> dispatcher, Integer trackLength, Consumer<Object> dispatchToApplyPower) {

        if (speed <= 0) {
            throw new IllegalArgumentException("Speed must be greater than 0");
        }

        this.dispatchToApplyPower = dispatchToApplyPower;
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

    public void move(double currentPosition) {
        MovementEvent movementEvent = new MovementEvent(this, GameEventName.RUNNING, this.speed, currentPosition);

        dispatcher.emmit(GameEventName.RUNNING, movementEvent);
    }

    @Override
    public void run() {
        double position = 0;

        LocalDateTime startMovementControlTime = LocalDateTime.now();
        LocalDateTime endMovementControlTime = LocalDateTime.now();
        LocalDateTime startPowerControlTime = LocalDateTime.now();
        LocalDateTime endPowerControlTime = LocalDateTime.now();
        LocalDateTime startPowerAttackControlTime = LocalDateTime.now();
        LocalDateTime endPowerAttackControlTime = LocalDateTime.now();

        while (position < trackLength) {
            if (ChronoUnit.MILLIS.between(startMovementControlTime, endMovementControlTime) >= Config.TIME_BETWEEN_EACH_MOVEMENT) {
                position += getSpeed();
                move(position);

                startMovementControlTime = endMovementControlTime;
            }

            if (ChronoUnit.MILLIS.between(startPowerControlTime, endPowerControlTime) >= Config.TIME_BETWEEN_EACH_POWER_UP && this.power == null) {
                this.dispatchToApplyPower.accept(this);

                startPowerControlTime = endPowerControlTime;
            }

            if (ChronoUnit.MILLIS.between(startPowerAttackControlTime, endPowerAttackControlTime) >= Config.TIME_BETWEEN_EACH_ATTACK) {
                Random random = new Random();
                int randomNumber = random.nextInt(100);

                if (randomNumber % 2 == 0) {
                    this.usePower();
                }

                startPowerAttackControlTime = endPowerAttackControlTime;
            }

            endPowerAttackControlTime = LocalDateTime.now();
            endPowerControlTime = LocalDateTime.now();
            endMovementControlTime = LocalDateTime.now();
        }

        LocalDateTime now = LocalDateTime.now();
        ZoneId zoneId = ZoneId.systemDefault();
        long epochSeconds = now.atZone(zoneId).toEpochSecond();

        RaceFinishEvent raceFinishEvent = new RaceFinishEvent(this, epochSeconds);
        getDispatcher().emmit(GameEventName.FINISHED, raceFinishEvent);
        finishRace = true;
    }

    @Override
    public String toString() {
        return "[CarThreadId: %d | Car Type: %s]".formatted(Thread.currentThread().threadId(), this.getClass().getSimpleName());
    }

    public Boolean isFinishedRace() {
        return this.finishRace;
    }

    public void increaseSpeed(double value) {
        double newSpeed = this.speed + value;
        if (newSpeed < 1.5) {
            this.speed = 1.5;
            return;
        }

        this.speed = newSpeed;
    }

    public void setPower(@Nullable Power power) {
        if (power == null) {
            return;
        }

        this.power = power;
    }

    public void usePower() {
        if (power != null) {
            System.out.printf("[%s] Using power [%s]%n", this, power.getClass().getSimpleName());

            power.activate();
            power = null;
        }
    }

    public String getActivePowerName() {
        return power != null ? power.getClass().getSimpleName() : null;
    }

    public boolean hasPower() {
        return power != null;
    }
}
