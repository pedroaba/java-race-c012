package pedroaba.java.race.ui;

import kotlin.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pedroaba.java.race.Beetle;
import pedroaba.java.race.Ferrari;
import pedroaba.java.race.constants.Config;
import pedroaba.java.race.entities.Car;
import pedroaba.java.race.entities.Race;
import pedroaba.java.race.enums.GameEventName;
import pedroaba.java.race.events.*;
import pedroaba.java.race.ui.fonts.SakanaFont;
import pedroaba.java.race.ui.images.*;
import pedroaba.java.race.utils.FormatEpochSecondToString;
import pedroaba.java.race.utils.Position;
import pedroaba.java.race.utils.Size;
import processing.core.PApplet;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class ThreadRacing extends PApplet {
    private final Map<Long, CarVisual> carVisuals = new ConcurrentHashMap<>();
    private final List<String> raceMessages = new ArrayList<>();
    private final List<RaceFinishEvent> finishEvents = Collections.synchronizedList(new ArrayList<>());
    private final AtomicInteger laneCounter = new AtomicInteger(0);
    private Race race;
    private Dispatcher<Object> dispatcher;
    private boolean raceFinished = false;
    private boolean countdownStarted = false;
    private int countdownValue = 3;
    private long lastCountdownTime = 0;
    private boolean showSemaphore = true;

    private Thread raceThread;

    private FerrariImage ferrariImg;
    private BeetleImage beetleImg;
    private LamborghiniImage lamboImg;
    private BananaImage bananaImg;
    private BoostImage boostImg;
    private ShellImage shellImg;

    private SakanaFont sakanaFont;

    @Override
    public void exit() {
        raceFinished = true;
        if (raceThread != null && raceThread.isAlive()) {
            raceThread.interrupt();
            try {
                raceThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        super.exit();
    }

    @Override
    public void settings() {
        size(Config.WIDTH, Config.HEIGHT);
    }

    @Override
    public void setup() {
        initializeApp();

        sakanaFont = new SakanaFont();
        textFont(sakanaFont.font());

        ferrariImg = new FerrariImage();
        beetleImg = new BeetleImage();
        lamboImg = new LamborghiniImage();

        bananaImg = new BananaImage();
        boostImg = new BoostImage();
        shellImg = new ShellImage();

        resizeCarImage(ferrariImg);
        resizeCarImage(beetleImg);
        resizeCarImage(lamboImg);

        resizePowerImage(shellImg);
        resizePowerImage(bananaImg);
        resizePowerImage(boostImg);

        setupRace();
    }

    @Override
    public void draw() {
        background(240);
        updateCarPositions();

        addAppTitle();

        if (countdownStarted) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastCountdownTime >= 1000) {
                countdownValue--;
                lastCountdownTime = currentTime;

                if (countdownValue < 1) {
                    countdownStarted = false;
                    showSemaphore = false;

                    raceThread = new Thread(() -> race.race());
                    raceThread.start();
                }
            }

            if (showSemaphore) {
                drawSemaphore(countdownValue);
            }
        }

        if (!countdownStarted) {
            drawTrack();
        }

        drawCars();

        if (raceFinished) {
            drawResults();
        }

        drawMessages();
    }

    public void addAppTitle() {
        float titleXPosition = (float) Config.WIDTH / 2;

        textFont(sakanaFont.font());
        fill(0);
        textSize(64);
        textAlign(CENTER, CENTER);
        text("Thread Racing", titleXPosition, 60);
        textFont(sakanaFont.font());
    }

    private void resizePowerImage(@Nullable Image powerImage) {
        try {
            if (powerImage != null) {
                powerImage.image().resize(30, 30);
            }
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void resizeCarImage(@Nullable Image carImage) {
        try {
            if (carImage != null) {
                carImage.image().resize(Config.CAR_WIDTH, Config.CAR_HEIGHT);
            }
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void initializeApp() {
        frameRate(60);
        background(0);
        smooth();
    }

    private void setupRace() {
        dispatcher = new Dispatcher<>("RaceDispatcher");
        setupListeners();

        // TODO - Make it imputable by user
        race = new Race(5, dispatcher, 100);

        countdownStarted = true;
        lastCountdownTime = System.currentTimeMillis();
    }

    private void setupListeners() {
        Listener<Object> startRaceListener = getRaceListener();
        Listener<Object> movementListener = getMovementListener();
        Listener<Object> finishListener = getFinishListener();
        Listener<Object> allFinishListener = getAllFinishListener();

        dispatcher.addListener(startRaceListener);
        dispatcher.addListener(movementListener);
        dispatcher.addListener(finishListener);
        dispatcher.addListener(allFinishListener);
    }

    private @NotNull Listener<Object> getAllFinishListener() {
        Listener<Object> allFinishListener = new Listener<>(GameEventName.RACE_FINISHED);
        allFinishListener.on((event) -> {
            AllCarFinishEvent allFinishEvent = (AllCarFinishEvent) event;
            String message = "Corrida finalizada: " + FormatEpochSecondToString.formatEpochSecond(allFinishEvent.finishTime());

            synchronized (raceMessages) {
                raceMessages.add(message);
                if (raceMessages.size() > 10) {
                    raceMessages.removeFirst();
                }
            }

            raceFinished = true;
        });
        return allFinishListener;
    }

    private @NotNull Listener<Object> getFinishListener() {
        Listener<Object> finishListener = new Listener<>(GameEventName.FINISHED);
        finishListener.on((event) -> {
            RaceFinishEvent finishEvent = (RaceFinishEvent) event;
            Car car = finishEvent.car();
            String message = "Carro " + car.getClass().getSimpleName() + " [" + car.threadId() + "] terminou!";

            synchronized (raceMessages) {
                raceMessages.add(message);
                if (raceMessages.size() > 10) {
                    raceMessages.removeFirst();
                }
            }

            finishEvents.add(finishEvent);
            CarVisual visual = carVisuals.get(car.threadId());
            if (visual != null) {
                visual.setFinished(true);
            }
        });
        return finishListener;
    }

    private @NotNull Listener<Object> getMovementListener() {
        Listener<Object> movementListener = new Listener<>(GameEventName.RUNNING);
        movementListener.on((event) -> {
            MovementEvent movementEvent = (MovementEvent) event;
            Car car = movementEvent.car();
            long threadId = car.threadId();

            if (!carVisuals.containsKey(threadId)) {
                CarVisual visual = getCarVisual(car, threadId, movementEvent);
                carVisuals.put(threadId, visual);
            }

            CarVisual visual = carVisuals.get(threadId);
            visual.setPosition(movementEvent.position());
            visual.setSpeed(movementEvent.speed());
            visual.setActivePower(car.getActivePowerName());
        });

        return movementListener;
    }

    private @NotNull Listener<Object> getRaceListener() {
        Listener<Object> startRaceListener = new Listener<>(GameEventName.RACE_STARTED);
        startRaceListener.on((event) -> {
            RaceStartedEvent startedEvent = (RaceStartedEvent) event;
            String message = "Corrida iniciada: " + FormatEpochSecondToString.formatEpochSecond(startedEvent.startTime());

            synchronized (raceMessages) {
                raceMessages.add(message);
                if (raceMessages.size() > 10) {
                    raceMessages.removeFirst();
                }
            }
        });
        return startRaceListener;
    }

    private @NotNull CarVisual getCarVisual(@NotNull Car car, long threadId, MovementEvent movementEvent) {
        Image carImage = switch (car) {
            case Beetle _ -> beetleImg;
            case Ferrari _ -> ferrariImg;
            default -> lamboImg;
        };

        int laneIndex = laneCounter.getAndIncrement();
        return new CarVisual(car.getClass().getSimpleName(), threadId, carImage.image(), laneIndex, movementEvent.speed());
    }

    private void drawTrack() {
        drawBottomOfTrack();
        drawStartingLine();
        drawFinishingLine();

        noStroke();
        for (int i = 0; i < carVisuals.size(); i++) {
            drawLane(i);

            drawDistanceMarkers(i);
            noStroke();
        }
    }

    private void drawBottomOfTrack() {
        fill(100);
        rect(50, Config.TRACK_Y_START - 20, Config.WIDTH - 100, Config.LANE_HEIGHT * carVisuals.size() + 40);
    }

    private void drawStartingLine() {
        stroke(255);
        line(60, Config.TRACK_Y_START - 20, 60, Config.TRACK_Y_START + Config.LANE_HEIGHT * carVisuals.size() + 20);
    }

    private void drawFinishingLine() {
        stroke(255);
        fill(255, 0, 0, 100);
        rect(Config.FINISH_LINE_X, Config.TRACK_Y_START - 20, 10, Config.LANE_HEIGHT * carVisuals.size() + 40);
    }

    private void drawLane(int laneIndex) {
        Boolean isEvenLane = laneIndex % 2 == 0;
        paintLaneBackground(isEvenLane);

        rect(60, Config.TRACK_Y_START + laneIndex * Config.LANE_HEIGHT, Config.FINISH_LINE_X - 60, Config.LANE_HEIGHT);
    }

    private void drawDistanceMarkers(int laneIndex) {
        stroke(255, 255, 255, 100);
        for (int mark = 0; mark < 10; mark++) {
            float markXPosition = 60 + (Config.FINISH_LINE_X - 60) * mark / 10.0f;
            line(markXPosition, Config.TRACK_Y_START + laneIndex * Config.LANE_HEIGHT, markXPosition, Config.TRACK_Y_START + (laneIndex + 1) * Config.LANE_HEIGHT);
        }
    }

    private void paintLaneBackground(@NotNull Boolean isEvenLane) {
        if (isEvenLane) {
            fill(100);
            return;
        }

        fill(90);
    }

    private void drawCars() {
        for (CarVisual carVisual : carVisuals.values()) {
            Position position = calculateCarPosition(carVisual);
            putShadowOnCar(position);
            rotateCar(carVisual, position);
            putNameAndVelocityOnCar(carVisual, position);

            if (carVisual.finished) {
                putFinishMark(position.x(), position.y());
            }

            if (carVisual.activePower != null && !carVisual.finished) {
                Image powerImg;

                switch (carVisual.activePower) {
                    case "Banana" -> powerImg = bananaImg;
                    case "Boost" -> powerImg = boostImg;
                    case "RedShell" -> powerImg = shellImg;
                    default -> {
                        continue;
                    }
                }

                text(powerImg.getName(), position.x(), position.y() - 25);
                image(powerImg.image(), position.x() - 25, position.y() - 15);
            }
        }
    }

    @Contract("_ -> new")
    private @NotNull Position calculateCarPosition(@NotNull CarVisual visual) {
        float trackLength = Config.FINISH_LINE_X - 60;
        float xPos = 60 + min((float) (visual.displayPosition / 100.0 * trackLength), trackLength);
        float yPos = Config.TRACK_Y_START + visual.laneIndex * Config.LANE_HEIGHT + (float) Config.LANE_HEIGHT / 2;

        return new Position(xPos, yPos);
    }

    private void putShadowOnCar(@NotNull Position position) {
        fill(0, 0, 0, 50);
        noStroke();
        ellipse(position.x() + 5, position.y() + 15, 50, 20);
    }

    private void rotateCar(@NotNull CarVisual car, @NotNull Position position) {
        pushMatrix();
        translate(position.x(), position.y());
        rotate(HALF_PI);
        imageMode(CENTER);
        image(car.image, 0, 0);
        popMatrix();
    }

    private void putNameAndVelocityOnCar(@NotNull CarVisual car, @NotNull Position position) {
        textAlign(LEFT, CENTER);
        textSize(15);
        fill(0);
        text(car.name + " [" + car.threadId + "]", position.x() + 50, position.y() - 10);
        text("Vel: " + nf(car.speed.floatValue(), 1, 2), position.x() + 50, position.y() + 8);
    }

    public void putFinishMark(float x, float y) {
        fill(0, 255, 0);
        ellipse(x + 30, y - 15, 15, 15);
        fill(0);
        textSize(10);
        textAlign(CENTER, CENTER);
        text("✓", x + 30, y - 15);
    }

    private void drawResults() {
        float rowHeight = 30f;
        var resultPanel = drawResultPanel(rowHeight);
        Position panelPosition = resultPanel.getFirst();
        Size headerPanelSize = resultPanel.getSecond();

        List<RaceFinishEvent> sortedEvents = new ArrayList<>(finishEvents);
        sortedEvents.sort(Comparator.comparing(RaceFinishEvent::finishTime));

        float startY = panelPosition.y() + headerPanelSize.height() + 15f;
        float posX = panelPosition.x() + 30f;
        float nameX = panelPosition.x() + 70f;
        float timeX = panelPosition.x() + headerPanelSize.width() - 80f;

        Position divisionLinePosition = new Position(-1f, startY);
        drawHeaderDivisionLine(divisionLinePosition, panelPosition, headerPanelSize);

        for (int i = 0; i < sortedEvents.size(); i++) {
            RaceFinishEvent event = sortedEvents.get(i);
            Car car = event.car();

            float yPos = startY + (i * rowHeight) + rowHeight / 2;
            Position position = new Position(posX, yPos);
            Position namePosition = new Position(nameX, yPos);
            Position timePosition = new Position(timeX, yPos);

            drawResultLine(car, i, rowHeight, position, panelPosition, namePosition, timePosition, headerPanelSize, event.getFinishTime());
        }
    }

    @Contract("_ -> new")
    private @NotNull Pair<Position, Size> drawResultPanel(float rowHeight) {
        Size headerPanelSize = new Size(400f, 50f);
        float panelHeight = headerPanelSize.height() + ((float) finishEvents.size() * rowHeight) + 20f;

        Size panelSize = new Size(400f, panelHeight);

        float panelX = (Config.WIDTH - panelSize.width()) / 2f;
        float panelY = (Config.HEIGHT - panelSize.height()) / 2f;

        Position panelPosition = new Position(panelX, panelY);

        drawPanelBackground(panelPosition, panelSize);
        drawPanelHeader(panelPosition, headerPanelSize);

        return new Pair<>(panelPosition, headerPanelSize);
    }

    private void drawPanelBackground(@NotNull Position position, @NotNull Size size) {
        fill(255, 255, 255, 230);
        stroke(50);
        strokeWeight(2);
        rect(position.x(), position.y(), size.width(), size.height(), 10);
        noStroke();
    }

    private void drawPanelHeader(@NotNull Position position, @NotNull Size size) {
        fill(30, 30, 120);
        rect(position.x(), position.y(), size.width(), size.height(), 10, 10, 0, 0);

        fill(255);
        textAlign(CENTER, CENTER);
        textSize(24);
        text("RESULTADOS", position.x() + size.width() / 2, position.y() + size.height() / 2);
    }

    private void drawHeaderDivisionLine(@NotNull Position position, @NotNull Position panelPosition, @NotNull Size size) {
        stroke(200);
        strokeWeight(1);
        line(panelPosition.x() + 20f, position.y(), panelPosition.x() + size.width() - 20f, position.y());
        noStroke();
    }

    private void drawResultLine(Car car, Integer carIndex, Float rowHeight, Position position, Position panelPosition, Position namePosition, Position timePosition, Size panelSize, Long finishTime) {
        if (carIndex == 0) {
            fill(255, 250, 200, 100);
            rect(panelPosition.x() + 10, position.y() - rowHeight / 2, panelSize.width() - 20, rowHeight, 5);
        } else if (carIndex % 2 == 1) {
            fill(240, 240, 240, 100);
            rect(panelPosition.x() + 10, position.y() - rowHeight / 2, panelSize.width() - 20, rowHeight);
        }

        int textColor;
        switch (carIndex) {
            case 0 -> textColor = color(180, 150, 0);
            case 1 -> textColor = color(120);
            case 2 -> textColor = color(150, 90, 30);
            default -> textColor = color(50);
        }

        fill(textColor);
        textAlign(CENTER, CENTER);
        textSize(16);
        text("%d º".formatted(carIndex + 1), position.x(), position.y());

        fill(0);
        textAlign(LEFT, CENTER);
        textSize(14);

        String carName = "%s [%d]".formatted(car.getName(), carIndex);
        text(carName, namePosition.x(), namePosition.y());

        textAlign(RIGHT, CENTER);
        text(FormatEpochSecondToString.formatEpochSecond(finishTime).substring(11), timePosition.x(), timePosition.y());
    }

    private void drawMessages() {
        fill(255, 255, 255, 200);
        rect(10, Config.HEIGHT - 30 - raceMessages.size() * 20, 450, 20 + raceMessages.size() * 20);

        fill(0);
        textAlign(LEFT);
        textSize(14);

        synchronized (raceMessages) {
            for (int i = 0; i < raceMessages.size(); i++) {
                text(raceMessages.get(i), 20, Config.HEIGHT - 20 - (raceMessages.size() - i - 1) * 20);
            }
        }
    }

    private void updateCarPositions() {
        for (CarVisual carVisual : carVisuals.values()) {
            carVisual.updateDisplayPosition();
        }
    }

    private void drawSemaphore(int countdownValue) {
        fill(50);
        stroke(20);
        strokeWeight(2);
        rect(((float) Config.WIDTH / 2) - 40, ((float) Config.HEIGHT / 2) - 120, 80, 240, 10);

        int lightRadius = 25;
        int lightSpacing = 70;
        int centerX = Config.WIDTH / 2;
        int startY = Config.HEIGHT / 2 - 80;

        if (countdownValue >= 3) {
            fill(255, 0, 0);
        } else {
            fill(100, 0, 0);
        }

        circle(centerX, startY, lightRadius * 2);

        if (countdownValue == 2) {
            fill(255, 255, 0);
        } else {
            fill(100, 100, 0);
        }

        circle(centerX, startY + lightSpacing, lightRadius * 2);

        if (countdownValue <= 1) {
            fill(0, 255, 0);
        } else {
            fill(0, 100, 0);
        }

        circle(centerX, startY + lightSpacing * 2, lightRadius * 2);

        noFill();
        strokeWeight(4);
        if (countdownValue >= 3) {
            stroke(255, 100, 100, 150);
            circle(centerX, startY, lightRadius * 2 + 10);
        } else if (countdownValue == 2) {
            stroke(255, 255, 100, 150);
            circle(centerX, startY + lightSpacing, lightRadius * 2 + 10);
        } else {
            stroke(100, 255, 100, 150);
            circle(centerX, startY + lightSpacing * 2, lightRadius * 2 + 10);
        }

        stroke(0);
        strokeWeight(1);
    }

}
