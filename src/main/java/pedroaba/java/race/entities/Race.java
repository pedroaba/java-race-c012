package pedroaba.java.race.entities;

import pedroaba.java.race.Beetle;
import pedroaba.java.race.Ferrari;
import pedroaba.java.race.Lamborghini;
import pedroaba.java.race.enums.GameEventName;
import pedroaba.java.race.events.AllCarFinishEvent;
import pedroaba.java.race.events.Dispatcher;
import pedroaba.java.race.events.Listener;
import pedroaba.java.race.events.RaceStartedEvent;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Race {
    private final int quantityOfCars;
    private final List<Car> cars = new ArrayList<>();
    private final Integer trackLength;

    private Boolean raceFinished = false;
    private final Dispatcher<Object> dispatcher;

    private Car winner = null;

    // Agora o construtor recebe o dispatcher e o listener para repassar aos carros
    public Race(int quantityOfCars, Dispatcher<Object> dispatcher, int trackLength) {
        this.quantityOfCars = quantityOfCars;
        this.trackLength = trackLength;
        this.dispatcher = dispatcher;

        for (int i = 0; i < quantityOfCars; i++) {
            int choice = new Random().nextInt(3);
            switch (choice) {
                case 0:
                    cars.add(new Beetle(dispatcher, trackLength));
                    break;
                case 1:
                    cars.add(new Ferrari(dispatcher, trackLength));
                    break;
                case 2:
                    cars.add(new Lamborghini(dispatcher, trackLength));
                    break;
            }
        }
    }

    public double getTrackLength() {
        return trackLength;
    }

    public boolean isRaceOver() {
        return raceFinished;
    }

    public int getQuantityOfCars() {
        return quantityOfCars;
    }

    public List<Car> getCars() {
        return cars;
    }

    public void race() {
        LocalDateTime now = LocalDateTime.now();
        ZoneId zone = ZoneId.systemDefault();
        long timestamp = now.atZone(zone).toEpochSecond();

        RaceStartedEvent startedEvent = new RaceStartedEvent(timestamp);
        this.dispatcher.emmit(GameEventName.RACE_STARTED, startedEvent);

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
    }
}
