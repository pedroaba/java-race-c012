package pedroaba.java.race.scheduler;

import pedroaba.java.race.entities.Car;
import java.util.Comparator;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Always picks the Car with the smallest pitStopTaskDuration.
 */
public class SjfScheduling implements SchedulingAlgorithm<Car> {
    @Override
    public Car next(@NotNull List<Car> items) {
        return items.stream()
                .min(Comparator.comparingInt(Car::getTaskDuration))
                .orElseThrow(() -> new IllegalStateException("No cars in queue"));
    }
}
