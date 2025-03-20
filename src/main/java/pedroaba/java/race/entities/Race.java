package pedroaba.java.race.entities;

import pedroaba.java.race.Beetle;

import java.util.ArrayList;
import java.util.List;

public class Race {
    private int quantityOfCars = 5;
    private final List<Car> cars = new ArrayList<>();

    public Race(int quantityOfCars) {
        this.quantityOfCars = quantityOfCars;

        for (int i = 0; i < quantityOfCars; i++) {
            cars.add(new Beetle());
        }
    }

    public int getQuantityOfCars() {
        return quantityOfCars;
    }

    public void race() {
        System.out.println("Racing " + getQuantityOfCars() + " cars");
    }
}
