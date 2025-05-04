package pedroaba.java.race.utils;

import pedroaba.java.race.constants.FeatureFlags;
import pedroaba.java.race.entities.Car;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Semaphore;

public class PitStopCarList {
    private final List<Car> cars = new ArrayList<>();
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private final Semaphore semaphore = new Semaphore(1, true);

    private final String propertyName = "size";

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(this.propertyName, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(this.propertyName, listener);
    }

    public void addCar(Car car) {
        try {
            if (FeatureFlags.applySemaphoreLogic) {
                semaphore.acquire();
            }

            Integer sizeOfListBefore = cars.size();
            cars.add(car);

            this.dispatch(sizeOfListBefore, cars.size());
        } catch (Exception e) {
            e.fillInStackTrace();
        } finally {
            if (FeatureFlags.applySemaphoreLogic) {
                semaphore.release();
            }
        }
    }

    public void removeCar(Car car) {
        try {
            if (FeatureFlags.applySemaphoreLogic) {
                semaphore.acquire();
            }

            Integer sizeOfListBefore = cars.size();
            cars.remove(car);

            this.dispatch(sizeOfListBefore, cars.size());
        } catch (Exception e) {
            e.fillInStackTrace();
        } finally {
            if (FeatureFlags.applySemaphoreLogic) {
                semaphore.release();
            }
        }
    }

    public List<Car> getCars() {
        return new ArrayList<>(cars);
    }

    private void dispatch(Integer oldSize, Integer newSize) {
        propertyChangeSupport.firePropertyChange(this.propertyName, Optional.of(oldSize), newSize);
    }
}
