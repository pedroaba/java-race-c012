package pedroaba.java.race;

import pedroaba.java.race.entities.Car;
import pedroaba.java.race.events.Dispatcher;
import pedroaba.java.race.scheduler.PitStopScheduler;

import java.util.Random;
import java.util.function.Consumer;

public class Beetle extends Car {
    private static final double DEFAULT_SPEED = 1.5;

    public Beetle(Dispatcher<Object> dispatcher, int trackLength, Consumer<Object> consumer, PitStopScheduler pitStopScheduler) {
        super(Beetle.DEFAULT_SPEED, dispatcher, trackLength, consumer, pitStopScheduler);
    }
}
