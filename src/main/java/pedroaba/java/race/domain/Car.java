package pedroaba.java.race.domain;

public abstract class Car {
    private double speed;
    private final double acceleration;

    public Car(double speed, double acceleration) {
        if (speed < 0 || acceleration < 0) {
            throw new IllegalArgumentException("Speed/Acceleration must be greater than 0");
        }

        this.speed = speed;
        this.acceleration = acceleration;
    }

    public void accelerate() {
        this.speed += acceleration;
    }

    public void decelerate() {
        this.speed -= acceleration;
    }

    public double move(double currentPosition) {
        if (currentPosition < speed) {
            return speed;
        }

        return currentPosition + speed;
    }

    public double getSpeed() {
        return speed;
    }

    public double getAcceleration() {
        return acceleration;
    }
}
