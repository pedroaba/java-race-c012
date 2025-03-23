package pedroaba.java.race;

import pedroaba.java.race.entities.Car;
import pedroaba.java.race.entities.Race;
import pedroaba.java.race.enums.GameEventName;
import pedroaba.java.race.events.*;
import pedroaba.java.race.utils.FormatEpochSecondToString;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadRacing extends PApplet {
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 800;
    private static final int TRACK_Y_START = 150;
    private static final int LANE_HEIGHT = 80;
    private static final int FINISH_LINE_X = 900;
    private static final int CAR_WIDTH = 40;
    private static final int CAR_HEIGHT = 82;

    private Race race;
    private Dispatcher<Object> dispatcher;
    private Map<Long, CarVisual> carVisuals = new ConcurrentHashMap<>();
    private List<String> raceMessages = new ArrayList<>();
    private boolean raceFinished = false;
    private List<RaceFinishEvent> finishEvents = Collections.synchronizedList(new ArrayList<>());
    private AtomicInteger laneCounter = new AtomicInteger(0);
    private boolean countdownStarted = false;
    private int countdownValue = 3;
    private long lastCountdownTime = 0;

    // Imagens
    private PImage ferrariImg;
    private PImage beetleImg;
    private PImage lamboImg;
    private PImage bananaImg;
    private PImage boostImg;
    private PImage shellImg;

    private PFont font;

    @Override
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    @Override
    public void setup() {
        frameRate(30);
        background(255);
        smooth();

        // Carregando fontes
        font = createFont("monospace", 14, true); // TODO: Colocar uma fonte binita
        textFont(font);

        ferrariImg = loadImage("src/images/Carro_1.png");  // Ferrari
        beetleImg = loadImage("src/images/Carro_2.png");   // Beetle
        lamboImg = loadImage("src/images/Carro_3.png");    // Lamborghini

        // Redimensionando imagens
        if (ferrariImg != null) ferrariImg.resize(CAR_WIDTH, CAR_HEIGHT);
        if (beetleImg != null) beetleImg.resize(CAR_WIDTH, CAR_HEIGHT);
        if (lamboImg != null) lamboImg.resize(CAR_WIDTH, CAR_HEIGHT);

        // Icones de poderes
        bananaImg = createPowerImage(color(255, 255, 0)); // Banana
        boostImg = createPowerImage(color(0, 0, 255));    // Boost
        shellImg = createPowerImage(color(255, 0, 0));    // Shell

        setupRace();
    }

    private PImage createPowerImage(int powerColor) {
        PImage img = createImage(20, 20, RGB);
        img.loadPixels();
        for (int i = 0; i < img.pixels.length; i++) {
            int x = i % 20;
            int y = i / 20;

            float dist = dist(x, y, 10, 10);
            if (dist < 8) {
                img.pixels[i] = powerColor;
            } else {
                img.pixels[i] = color(0, 0, 0, 0); // Transparente
            }
        }
        img.updatePixels();
        return img;
    }

    private void setupRace() {
        dispatcher = new Dispatcher<>("RaceDispatcher");

        // Cria listeners para eventos da corrida
        setupListeners();

        // Corrida com 5 carros e comprimento de pista 100
        race = new Race(5, dispatcher, 100);

        // Inicia o countdown
        countdownStarted = true;
        lastCountdownTime = System.currentTimeMillis();
    }

    private void setupListeners() {
        // Listener para início de corrida
        Listener<Object> startRaceListener = new Listener<>(GameEventName.RACE_STARTED);
        startRaceListener.on((event) -> {
            RaceStartedEvent startedEvent = (RaceStartedEvent) event;
            String message = "Corrida iniciada: " + FormatEpochSecondToString.formatEpochSecond(startedEvent.startTime());

            synchronized (raceMessages) {
                raceMessages.add(message);
                if (raceMessages.size() > 10) {
                    raceMessages.remove(0);
                }
            }
        });

        // Listener para movimento dos carros
        Listener<Object> movementListener = new Listener<>(GameEventName.RUNNING);
        movementListener.on((event) -> {
            MovementEvent movementEvent = (MovementEvent) event;
            Car car = movementEvent.car();
            long threadId = car.threadId();

            // Cria visualização do carro se não existir
            if (!carVisuals.containsKey(threadId)) {
                PImage carImage;
                if (car instanceof Beetle) {
                    carImage = beetleImg;
                } else if (car instanceof Ferrari) {
                    carImage = ferrariImg;
                } else {
                    carImage = lamboImg;
                }

                int laneIndex = laneCounter.getAndIncrement();
                CarVisual visual = new CarVisual(
                        car.getClass().getSimpleName(),
                        threadId,
                        carImage,
                        laneIndex,
                        movementEvent.speed()
                );
                carVisuals.put(threadId, visual);
            }

            // Atualizando a posição do carro
            CarVisual visual = carVisuals.get(threadId);
            visual.setPosition(movementEvent.position());
            visual.setSpeed(movementEvent.speed());
        });

        // Listener para fim de corrida de um carro
        Listener<Object> finishListener = new Listener<>(GameEventName.FINISHED);
        finishListener.on((event) -> {
            RaceFinishEvent finishEvent = (RaceFinishEvent) event;
            Car car = finishEvent.car();
            String message = "Carro " + car.getClass().getSimpleName() + " [" + car.threadId() + "] terminou!";

            synchronized (raceMessages) {
                raceMessages.add(message);
                if (raceMessages.size() > 10) {
                    raceMessages.remove(0);
                }
            }

            finishEvents.add(finishEvent);

            // Marca o carro como finalizado
            CarVisual visual = carVisuals.get(car.threadId());
            if (visual != null) {
                visual.setFinished(true);
            }
        });

        // Listener para todos os carros terminarem
        Listener<Object> allFinishListener = new Listener<>(GameEventName.RACE_FINISHED);
        allFinishListener.on((event) -> {
            AllCarFinishEvent allFinishEvent = (AllCarFinishEvent) event;
            String message = "Corrida finalizada: " +
                    FormatEpochSecondToString.formatEpochSecond(allFinishEvent.finishTime());

            synchronized (raceMessages) {
                raceMessages.add(message);
                if (raceMessages.size() > 10) {
                    raceMessages.remove(0);
                }
            }

            raceFinished = true;
        });

        dispatcher.addListener(startRaceListener);
        dispatcher.addListener(movementListener);
        dispatcher.addListener(finishListener);
        dispatcher.addListener(allFinishListener);
    }

    @Override
    public void draw() {
        background(240);

        // Titulo
        fill(0);
        textSize(32);
        textAlign(CENTER, CENTER);
        text("Thread Racing", WIDTH / 2, 40);

        // Countdown
        if (countdownStarted) {
            long currentTime = System.currentTimeMillis();

            // Atualiza a cada segundo
            if (currentTime - lastCountdownTime >= 1000) {
                countdownValue--;
                lastCountdownTime = currentTime;

                // Inicia a corrida quando chegar em 0
                if (countdownValue < 0) {
                    countdownStarted = false;

                    // Inicia a corrida em uma thread separada para não bloquear o Processing :v
                    new Thread(() -> race.race()).start();
                }
            }

            // Desenha o countdown no centro da tela
            if (countdownValue >= 0) {
                fill(255, 0, 0);
                textSize(120);
                textAlign(CENTER, CENTER);

                if (countdownValue == 0) {
                    text("GO!", WIDTH / 2, HEIGHT / 2);
                } else {
                    text(countdownValue, WIDTH / 2, HEIGHT / 2);
                }
            }
        } else {
            drawTrack();
        }

        // Desenha carros
        drawCars();

        // Desenha resultados
        if (raceFinished) {
            drawResults();
        }

        // Desenha log da corrida
        drawMessages();
    }

    private void drawTrack() {
        // Fundo da pista
        fill(100);
        rect(50, TRACK_Y_START - 20, WIDTH - 100, LANE_HEIGHT * carVisuals.size() + 40);

        // Linha de largada
        stroke(255);
        line(60, TRACK_Y_START - 20, 60, TRACK_Y_START + LANE_HEIGHT * carVisuals.size() + 20);

        // Linha de chegada
        stroke(255);
        fill(255, 0, 0, 100);
        rect(FINISH_LINE_X, TRACK_Y_START - 20, 10, LANE_HEIGHT * carVisuals.size() + 40);

        // Lanes
        noStroke();
        for (int i = 0; i < carVisuals.size(); i++) {
            if (i % 2 == 0) {
                fill(100);
            } else {
                fill(90);
            }
            rect(60, TRACK_Y_START + i * LANE_HEIGHT, FINISH_LINE_X - 60, LANE_HEIGHT);

            // Marcadores de distancia
            stroke(255, 255, 255, 100);
            for (int mark = 0; mark < 10; mark++) {
                float x = 60 + (FINISH_LINE_X - 60) * mark / 10.0f;
                line(x, TRACK_Y_START + i * LANE_HEIGHT,
                        x, TRACK_Y_START + (i + 1) * LANE_HEIGHT);
            }
            noStroke();
        }
    }

    private void drawCars() {
        for (CarVisual carVisual : carVisuals.values()) {
            // Calcula X baseada na posição da corrida (0 a 100)
            float trackLength = FINISH_LINE_X - 60;
            float xPos = 60 + min((float)(carVisual.position / 100.0 * trackLength), trackLength);
            float yPos = TRACK_Y_START + carVisual.laneIndex * LANE_HEIGHT + LANE_HEIGHT / 2;

            // Sombra do carro
            fill(0, 0, 0, 50);
            noStroke();
            ellipse(xPos + 5, yPos + 15, 50, 20);

            // Desenha o carro rotacionado para a direita
            pushMatrix();
            translate(xPos, yPos);
            rotate(HALF_PI); // Rotaciona 90 graus (sprite aponta para cima, queremos para a direita)
            imageMode(CENTER);
            image(carVisual.image, 0, 0);
            popMatrix();

            // Nome e Velocidade ao lado do carro
            textAlign(LEFT, CENTER);
            textSize(14);
            fill(0);
            text(carVisual.name + " [" + carVisual.threadId + "]", xPos + 50, yPos - 15);
            text("Vel: " + nf((float)carVisual.speed, 1, 2), xPos + 50, yPos + 5);

            // Marcador qunado terminar
            if (carVisual.finished) {
                fill(0, 255, 0);
                ellipse(xPos + 30, yPos - 15, 15, 15);
                fill(0);
                textSize(10);
                textAlign(CENTER, CENTER);
                text("✓", xPos + 30, yPos - 15);
            }

            // TODO: desenhar com base nos poderes
            if (random(1) < 0.05 && !carVisual.finished) {
                PImage powerImg;
                int powerType = (int)random(3);

                if (powerType == 0) {
                    powerImg = bananaImg;
                    fill(255, 255, 0);
                    text("Banana!", xPos, yPos - 25);
                } else if (powerType == 1) {
                    powerImg = boostImg;
                    fill(0, 0, 255);
                    text("Boost!", xPos, yPos - 25);
                } else {
                    powerImg = shellImg;
                    fill(255, 0, 0);
                    text("Red Shell!", xPos, yPos - 25);
                }

                image(powerImg, xPos - 25, yPos - 15);
            }
        }
    }

    private void drawResults() {
        fill(255, 255, 255, 200);
        rect(WIDTH - 300, 80, 270, 30 + finishEvents.size() * 20);

        fill(0);
        textAlign(LEFT);
        textSize(16);
        text("Resultados:", WIDTH - 280, 100);

        List<RaceFinishEvent> sortedEvents = new ArrayList<>(finishEvents);
        sortedEvents.sort(Comparator.comparing(RaceFinishEvent::finishTime));

        for (int i = 0; i < sortedEvents.size(); i++) {
            RaceFinishEvent event = sortedEvents.get(i);
            Car car = event.car();
            fill(0);
            textSize(12);
            text((i+1) + "º - " + car.getClass().getSimpleName() +
                    " [" + car.threadId() + "]", WIDTH - 280, 120 + i * 20);
        }
    }

    private void drawMessages() {
        // Mensagens da corrida no canto inferior esquerdo
        fill(255, 255, 255, 200);
        rect(10, HEIGHT - 30 - raceMessages.size() * 20, 450, 20 + raceMessages.size() * 20);

        fill(0);
        textAlign(LEFT);
        textSize(14);
        synchronized (raceMessages) {
            for (int i = 0; i < raceMessages.size(); i++) {
                text(raceMessages.get(i), 20, HEIGHT - 20 - (raceMessages.size() - i - 1) * 20);
            }
        }
    }

    private class CarVisual {
        private String name;
        private long threadId;
        private PImage image;
        private int laneIndex;
        private double position = 0;
        private double speed;
        private boolean finished = false;

        public CarVisual(String name, long threadId, PImage image, int laneIndex, double speed) {
            this.name = name;
            this.threadId = threadId;
            this.image = image;
            this.laneIndex = laneIndex;
            this.speed = speed;
        }

        public void setPosition(double position) {
            this.position = position;
        }

        public void setSpeed(double speed) {
            this.speed = speed;
        }

        public void setFinished(boolean finished) {
            this.finished = finished;
        }
    }
}