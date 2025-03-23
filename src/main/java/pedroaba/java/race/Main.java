package pedroaba.java.race;

import pedroaba.java.race.entities.Race;
import pedroaba.java.race.enums.GameEventName;
import pedroaba.java.race.events.*;
import pedroaba.java.race.utils.FormatEpochSecondToString;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) {
        List<RaceFinishEvent> raceFinishEvents = new ArrayList<>();
        Dispatcher<Object> dispatcher = new Dispatcher<>("RaceDispatcher");

        Listener<Object> startRaceListener = new Listener<>(GameEventName.RACE_STARTED);
        startRaceListener.on((event) -> {
            RaceStartedEvent startedEvent = (RaceStartedEvent) event;

            System.out.printf("Race Started: %s%n", FormatEpochSecondToString.formatEpochSecond(startedEvent.startTime()));
        });

        Listener<Object> listener = new Listener<>(GameEventName.RUNNING);
        listener.on(System.out::println);

        Listener<Object> raceFinishEventListener = new Listener<>(GameEventName.FINISHED);
        raceFinishEventListener.on((event) -> {
            RaceFinishEvent raceFinishEvent = (RaceFinishEvent) event;
            raceFinishEvents.add(raceFinishEvent);

            System.out.println(
              "Car: " + raceFinishEvent.getCar().getClass().getSimpleName()
                      + " - " + raceFinishEvent.getCar().threadId()
                      + " | Finished: " + FormatEpochSecondToString.formatEpochSecond(raceFinishEvent.getFinishTime())
            );
        });

        Listener<Object> allCarFinishEventListener = new Listener<>(GameEventName.RACE_FINISHED);
        allCarFinishEventListener.on((event) -> {
            AllCarFinishEvent allCarFinishEvent = (AllCarFinishEvent) event;

            System.out.println("All cars finished");
            System.out.println("Race finished on: " + FormatEpochSecondToString.formatEpochSecond(allCarFinishEvent.getFinishTime()));

            List<RaceFinishEvent> sortedRaceFinishEvents = raceFinishEvents.stream().sorted(Comparator.comparing(RaceFinishEvent::getFinishTime)).toList();
            final AtomicInteger listIndex = new AtomicInteger();

            sortedRaceFinishEvents.forEach(raceFinishEvent -> {
                final int index = listIndex.getAndIncrement();
                final String carName = "%s [%d]".formatted(raceFinishEvent.car().getClass().getSimpleName(), raceFinishEvent.car().threadId());

                System.out.printf(
                    "Car: %s - finished in %d° | race duration: %s%n",
                    carName,
                    index + 1,
                    FormatEpochSecondToString.formatEpochSecond(raceFinishEvent.getFinishTime())
                );
            });
        });

        dispatcher.addListener(listener);
        dispatcher.addListener(raceFinishEventListener);
        dispatcher.addListener(allCarFinishEventListener);
        dispatcher.addListener(startRaceListener);

        Race race = new Race(5, dispatcher, 100);
        race.race();
    }
}