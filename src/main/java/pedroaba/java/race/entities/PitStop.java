package pedroaba.java.race.entities;

import pedroaba.java.race.constants.Config;
import pedroaba.java.race.enums.GameEventName;
import pedroaba.java.race.events.*;
import pedroaba.java.race.utils.MakeUncheckedCasting;
import pedroaba.java.race.utils.PitStopCarList;
import pedroaba.java.race.utils.Sleeper;

public class PitStop<T> extends Thread {
    private final Dispatcher<T> dispatcher;
    private final Listener<T> raceFinishedListener;
    private final Listener<T> carEnterOnPitStopListener;
    private final Listener<T> carExitOnPitStopListener;

    private final Integer intervalOfPitStop;
    private Boolean isRaceFinished = false;

    private final PitStopCarList pitStopCarList = new PitStopCarList();

    public PitStop(Dispatcher<T> dispatcher) {
        this.dispatcher = dispatcher;
        this.raceFinishedListener = new Listener<>(GameEventName.RACE_FINISHED);
        this.raceFinishedListener.on((_) -> this.onRaceFinished());

        this.carEnterOnPitStopListener = new Listener<>(GameEventName.ENTER_IN_PIT_STOP);
        this.carEnterOnPitStopListener.on(this::onCarEnterOnPitStop);

        this.carExitOnPitStopListener = new Listener<>(GameEventName.EXIT_IN_PIT_STOP);
        this.carExitOnPitStopListener.on(this::onCarExitOnPitStop);

        this.pitStopCarList.addPropertyChangeListener(event -> {
            if (event.getPropertyName().equals("size")) {
                Integer currentCarsListSize = (Integer) event.getNewValue();
                if (currentCarsListSize != null && currentCarsListSize > Config.MAX_OF_CARS_IN_PIT_STOP) {
                    Integer quantityOfCars = (int) event.getNewValue();
                    this.dispatcher.emmit(
                        GameEventName.COLLISION_IN_PIT_STOP,
                        MakeUncheckedCasting.cast(
                            new PitStopCollisionEvent(quantityOfCars, pitStopCarList.getCars())
                        )
                    );
                }
            }
        });

        intervalOfPitStop = Config.INTERVAL_OF_PIT_STOP;
    }

    private void onRaceFinished() {
        this.isRaceFinished = true;
    }

    private void onCarEnterOnPitStop(T params) {
        CarEnterInPitStop carEnterInPitStop = (CarEnterInPitStop) params;
        this.pitStopCarList.addCar(carEnterInPitStop.car());
    }

    private void onCarExitOnPitStop(T params) {
        CarExitInPitStop carExitInPitStop = (CarExitInPitStop) params;
        this.pitStopCarList.removeCar(carExitInPitStop.car());
    }

    @Override
    public void run() {
        this.dispatcher.addListener(raceFinishedListener);
        this.dispatcher.addListener(carEnterOnPitStopListener);
        this.dispatcher.addListener(carExitOnPitStopListener);

        try {
            Sleeper.sleep(Integer.toUnsignedLong(intervalOfPitStop * 1000));
            while (this.isRaceFinished == false) {
                if (Thread.currentThread().isInterrupted()) {
                    this.isRaceFinished = true;
                    break;
                }

                dispatcher.emmit(GameEventName.STOP_IN_PIT_STOP, null);
                Sleeper.sleep(Integer.toUnsignedLong(intervalOfPitStop * 1000));
            }

            this.interrupt();
        } catch (Exception e) {
            e.fillInStackTrace();
            Thread.currentThread().interrupt();
        }
    }
}
