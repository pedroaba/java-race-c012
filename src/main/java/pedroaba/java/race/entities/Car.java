package pedroaba.java.race.entities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pedroaba.java.race.constants.Config;
import pedroaba.java.race.constants.FeatureFlags;
import pedroaba.java.race.enums.GameEventName;
import pedroaba.java.race.events.*;
import pedroaba.java.race.powers.Power;
import pedroaba.java.race.scheduler.PitStopScheduler;
import pedroaba.java.race.utils.ConverterUnit;
import pedroaba.java.race.utils.MechanicTasksGenerator;
import pedroaba.java.race.utils.Sleeper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public abstract class Car extends Thread {
    private final Dispatcher<Object> dispatcher;
    private final Integer trackLength;
    private final Consumer<Object> dispatchToApplyPower;
    private final Semaphore semaphore = new Semaphore(0);
    private final PitStopScheduler pitStopScheduler;
    private Power power;
    private double speed;
    private Boolean finishRace = false;
    private Boolean isInPitStop = false;
    private Boolean hasCollision = false;
    private Integer pitStopTaskDuration;

    private LocalDateTime pitStopStartWaitingTime;

    public Car(double speed, Dispatcher<Object> dispatcher, Integer trackLength, Consumer<Object> dispatchToApplyPower, PitStopScheduler pitStopScheduler) {
        if (speed <= 0) {
            throw new IllegalArgumentException("Speed must be greater than 0");
        }

        this.pitStopScheduler = pitStopScheduler;
        this.dispatchToApplyPower = dispatchToApplyPower;
        this.trackLength = trackLength;
        this.speed = speed;
        this.dispatcher = dispatcher;

        Listener<Object> pitStopListener = new Listener<>(GameEventName.STOP_IN_PIT_STOP);
        pitStopListener.on((_) -> this.onMustStopOnPitStop());

        Listener<Object> collisionListener = new Listener<>(GameEventName.COLLISION_IN_PIT_STOP);
        collisionListener.on((_) -> this.onCollisionPitStop());

        this.dispatcher.addListener(pitStopListener);
        this.dispatcher.addListener(collisionListener);
    }

    private void onCollisionPitStop() {
        if (this.isFinishedRace()) {
            return;
        }

        this.hasCollision = true;
    }

    private void onMustStopOnPitStop() {
        if (this.isFinishedRace()) {
            return;
        }

        this.pitStopTaskDuration = this.getPitStopTaskDuration();

        this.isInPitStop = true;
        System.out.printf("%s enter on pit stop - task duration '%d'%n", this, this.pitStopTaskDuration);
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
            if (this.hasCollision) {
                break;
            }

            if (Thread.currentThread().isInterrupted()) {
                break;
            }

            if (isInPitStop) {
                if (FeatureFlags.applyFCFSSchedulingAlgorithm || FeatureFlags.applySJFSchedulingAlgorithm) {
                    this.pitStopStartWaitingTime = LocalDateTime.now();
                    this.pitStopScheduler.register(this);
                    this.semaphore.acquireUninterruptibly();
                }

                if (FeatureFlags.applyFCFSSchedulingAlgorithm || FeatureFlags.applySJFSchedulingAlgorithm) {
                    Duration durationOfWait = Duration.between(pitStopStartWaitingTime, LocalDateTime.now());

                    System.out.println("======================== Waiting Time ========================");
                    System.out.printf("%s - Waiting for %d ms | task duration to do - %d %n", this, durationOfWait.toMillis(), this.getTaskDuration());
                    System.out.println("==============================================================");
                }

                String task = MechanicTasksGenerator.getTaskOnPitStop();
                this.dispatcher.emmit(GameEventName.ENTER_IN_PIT_STOP, new CarEnterInPitStop(this));
                System.out.printf("%s - Initializing: %s | Duration: %d %n", this, task, this.pitStopTaskDuration);
                Sleeper.sleep(Integer.toUnsignedLong(ConverterUnit.toMillis(this.pitStopTaskDuration)));
                System.out.printf("%s - Finishing: %s | Duration: %d s %n", this, task, this.pitStopTaskDuration);
                this.dispatcher.emmit(GameEventName.EXIT_IN_PIT_STOP, new CarExitInPitStop(this));

                if (FeatureFlags.applyFCFSSchedulingAlgorithm || FeatureFlags.applySJFSchedulingAlgorithm) {
                    this.pitStopScheduler.notifyExit();
                }

                this.isInPitStop = false;
            }

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
        this.dispatcher.emmit(GameEventName.FINISHED, raceFinishEvent);
        this.finishRace = true;
    }

    @Override
    public String toString() {
        return "[Id: %d | CarT: %s]".formatted(this.threadId(), this.getClass().getSimpleName());
    }

    public Integer getTaskDuration() {
        return pitStopTaskDuration;
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

    public void grantPermit() {
        semaphore.release();
    }

    private @NotNull Integer getPitStopTaskDuration() {
        return ThreadLocalRandom.current().nextInt(Config.PIT_STOP_DURATION_INTERVAL_RANGE.getFirst(), Config.PIT_STOP_DURATION_INTERVAL_RANGE.getSecond());
    }
}
