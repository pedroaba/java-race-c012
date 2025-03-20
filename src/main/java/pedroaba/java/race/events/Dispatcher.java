package pedroaba.java.race.events;

import pedroaba.java.race.enums.GameEventName;

import java.util.ArrayList;
import java.util.List;

public class Dispatcher<T> {
    private static Integer globalCount = 0;
    private final Integer id = ++globalCount;
    private final String name;

    private final List<Listener<T>> listeners = new ArrayList<>();

    public Dispatcher(String name) {
        this.name = name;
    }

    public void addListener(Listener<T> listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener<T> listener) {
        listeners.remove(listener);
    }

    public void emmit(GameEventName eventName, T params) {
        for (Listener<T> listener : listeners) {
            if (listener != null && listener.isEvent(eventName)) {
                listener.dispatch(params);
            }
        }
    }

    @Override
    public String toString() {
        return "[Dispatcher {%d}] %s".formatted(id, name);
    }
}
