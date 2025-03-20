package pedroaba.java.race.events;

import pedroaba.java.race.enums.GameEventName;

import java.util.function.Consumer;

public class Listener<T> {
    private static Integer globalCounter = 0;
    private Consumer<T> callback;

    private final Integer id = ++globalCounter;
    private final GameEventName eventName;

    public Listener(GameEventName eventName) {
        this.eventName = eventName;
    }

    public void on(Consumer<T> callback) {
        this.callback = callback;
    }

    public void dispatch(T params) {
        if (callback != null) {
            callback.accept(params);
        }
    }

    public boolean isEvent(GameEventName eventName) {
        return this.eventName.equals(eventName);
    }

    @Override
    public String toString() {
        return "[Listener {%d}] %s".formatted(id, eventName);
    }
}
