package pedroaba.java.race.scheduler;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FcfsScheduling<T> implements SchedulingAlgorithm<T> {
    @Override
    public T next(@NotNull List<T> items) {
        return items.getFirst();
    }
}

