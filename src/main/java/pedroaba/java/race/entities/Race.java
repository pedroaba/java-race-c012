package pedroaba.java.race.entities;

import org.jetbrains.annotations.Nullable;
import pedroaba.java.race.Beetle;
import pedroaba.java.race.Ferrari;
import pedroaba.java.race.Lamborghini;
import pedroaba.java.race.constants.FeatureFlags;
import pedroaba.java.race.enums.GameEventName;
import pedroaba.java.race.events.AllCarFinishEvent;
import pedroaba.java.race.events.Dispatcher;
import pedroaba.java.race.events.RaceStartedEvent;
import pedroaba.java.race.scheduler.FcfsScheduling;
import pedroaba.java.race.scheduler.PitStopScheduler;
import pedroaba.java.race.scheduler.SchedulingAlgorithm;
import pedroaba.java.race.scheduler.SjfScheduling;
import pedroaba.java.race.utils.ApplyPowerTo;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CyclicBarrier;

public class Race {
    private final List<Car> cars = new ArrayList<>();
    private final Dispatcher<Object> dispatcher;

    public Race(int quantityOfCars, Dispatcher<Object> dispatcher, int trackLength) {
        this.dispatcher = dispatcher;

        PitStopScheduler pitStopScheduler = getPitStopScheduler();

        for (int i = 0; i < quantityOfCars; i++) {
            int choice = new Random().nextInt(3);
            switch (choice) {
                case 0:
                    cars.add(
                        new Beetle(
                            dispatcher,
                            trackLength,
                            (event) -> this.applyPower((Car) event),
                                pitStopScheduler
                        )
                    );
                    break;
                case 1:
                    cars.add(
                        new Ferrari(
                            dispatcher,
                            trackLength,
                            (event) -> this.applyPower((Car) event),
                                pitStopScheduler
                        )
                    );
                    break;
                case 2:
                    cars.add(
                        new Lamborghini(
                            dispatcher,
                            trackLength,
                            (event) -> this.applyPower((Car) event),
                                pitStopScheduler
                        )
                    );
                    break;
            }
        }
    }

    private static @Nullable PitStopScheduler getPitStopScheduler() {
        SchedulingAlgorithm<Car> schedulingAlgorithm = null;
        if (FeatureFlags.applySJFSchedulingAlgorithm) {
            schedulingAlgorithm = new SjfScheduling();
        } else if (FeatureFlags.applyFCFSSchedulingAlgorithm) {
            schedulingAlgorithm = new FcfsScheduling<>();
        }

        PitStopScheduler pitStopScheduler = null;
        if (FeatureFlags.applyFCFSSchedulingAlgorithm || FeatureFlags.applySJFSchedulingAlgorithm) {
            pitStopScheduler = new PitStopScheduler(schedulingAlgorithm);
        }

        return pitStopScheduler;
    }

    private void applyPower(Car carToAddPower) {
        List<Car> filteredListToRemoveCarThatReceiveThePower = cars.stream().filter(c -> !c.equals(carToAddPower) && !c.isFinishedRace()).toList();
        if (filteredListToRemoveCarThatReceiveThePower.isEmpty()) {
            ApplyPowerTo.applyBoost(carToAddPower);

            return;
        }

        Random random =  new Random();

        random.setSeed(System.currentTimeMillis());
        int indexOfTargetCar = random.nextInt(filteredListToRemoveCarThatReceiveThePower.size());

        ApplyPowerTo.apply(carToAddPower, cars.get(indexOfTargetCar));
    }

    public void race() {
        LocalDateTime now = LocalDateTime.now();
        ZoneId zone = ZoneId.systemDefault();
        long timestamp = now.atZone(zone).toEpochSecond();

        RaceStartedEvent startedEvent = new RaceStartedEvent(timestamp);
        this.dispatcher.emmit(GameEventName.RACE_STARTED, startedEvent);

        PitStop<Object> pitStop = new PitStop<>(this.dispatcher);
        pitStop.start();

        for (Car car : cars) {
            car.start();
        }

        for (Car car : cars) {
            try {
                car.join();
            } catch (InterruptedException e) {
                e.fillInStackTrace();
            }
        }

        now = LocalDateTime.now();
        zone = ZoneId.systemDefault();
        timestamp = now.atZone(zone).toEpochSecond();

        AllCarFinishEvent event = new AllCarFinishEvent(timestamp);
        this.dispatcher.emmit(GameEventName.RACE_FINISHED, event);

        try {
            pitStop.join();
        } catch (InterruptedException e) {
            e.fillInStackTrace();
        }
    }
}
