package pedroaba.java.race.utils;

import pedroaba.java.race.entities.Car;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class PitStopCarList {
    private final List<Car> cars = new ArrayList<>();
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private final String propertyName = "size";

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(this.propertyName, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(this.propertyName, listener);
    }

    public synchronized void addCar(Car car) {
        Integer sizeOfListBefore = cars.size();
        cars.add(car);

        this.dispatch(sizeOfListBefore, cars.size());
    }

    public synchronized void removeCar(Car car) {
        Integer sizeOfListBefore = cars.size();
        cars.remove(car);

        this.dispatch(sizeOfListBefore, cars.size());
    }

    public synchronized List<Car> getCars() {
        return new ArrayList<>(cars);
    }

    private synchronized void dispatch(Integer oldSize, Integer newSize) {
        propertyChangeSupport.firePropertyChange(this.propertyName, Optional.of(oldSize), newSize);
    }
}
