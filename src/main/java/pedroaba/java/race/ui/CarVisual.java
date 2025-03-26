package pedroaba.java.race.ui;

import processing.core.PApplet;
import processing.core.PImage;

public class CarVisual extends PApplet {
    public String name;
    public long threadId;
    public PImage image;
    public int laneIndex;
    public double position = 0;
    public double speed;
    public boolean finished = false;
    public String activePower = null;
    private double targetPosition = 0;
    public double displayPosition = 0;
    private float lerpFactor = 0.1f;

    public CarVisual(String name, long threadId, PImage image, int laneIndex, double speed) {
        this.name = name;
        this.threadId = threadId;
        this.image = image;
        this.laneIndex = laneIndex;
        this.speed = speed;
    }

    public void setPosition(double position) {
        this.position = position;
        this.targetPosition = position;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public void setActivePower(String powerName) {
        this.activePower = powerName;
    }

    public void updateDisplayPosition() {
        this.displayPosition = lerp((float) this.displayPosition, (float) this.targetPosition, this.lerpFactor);
    }
}
