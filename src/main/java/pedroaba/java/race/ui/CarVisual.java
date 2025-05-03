package pedroaba.java.race.ui;

import processing.core.PApplet;
import processing.core.PImage;

public class CarVisual extends PApplet {
    public String name;
    public Long threadId;
    public PImage image;
    public Integer laneIndex;
    public Double position = 0.0;
    public Double speed;
    public Boolean finished = false;
    public String activePower = null;
    private Double targetPosition = 0.0;
    public Double displayPosition = 0.0;

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
        float lerpFactor = 0.1f;

        this.displayPosition = (double) lerp(this.displayPosition.floatValue(), this.targetPosition.floatValue(), lerpFactor);
    }
}
