package pedroaba.java.race.scheduler;

import java.util.List;

public interface SchedulingAlgorithm<T> {
    T next(List<T> items);
}
