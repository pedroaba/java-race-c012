package pedroaba.java.race.ui;

import pedroaba.java.race.Beetle;
import pedroaba.java.race.Ferrari;
import pedroaba.java.race.constants.Config;
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
        size(Config.WIDTH, Config.HEIGHT);
    }

    @Override
    public void setup() {
        frameRate(60);
        background(0);
        smooth();

        // Carregando fontes
        font = createFont("monospace", 14, true); // TODO: Colocar uma fonte binita
        textFont(font);

        ferrariImg = loadImage("src/images/Carro_1.png");  // Ferrari
        beetleImg = loadImage("src/images/Carro_2.png");   // Beetle
        lamboImg = loadImage("src/images/Carro_3.png");    // Lamborghini

        // Redimensionando imagens
        if (ferrariImg != null) ferrariImg.resize(Config.CAR_WIDTH, Config.CAR_HEIGHT);
        if (beetleImg != null) beetleImg.resize(Config.CAR_WIDTH, Config.CAR_HEIGHT);
        if (lamboImg != null) lamboImg.resize(Config.CAR_WIDTH, Config.CAR_HEIGHT);

        // Icones de poderes
        bananaImg = createPowerImage(color(255, 255, 0)); // Banana
        boostImg = createPowerImage(color(0, 0, 255));    // Boost
        shellImg = createPowerImage(color(255, 0, 0));    // Shell

        setupRace();
    }

    @Override
    public void draw() {
        background(240);
        updateCarPositions();

        // Titulo
        fill(0);
        textSize(32);
        textAlign(CENTER, CENTER);
        text("Thread Racing", Config.WIDTH / 2, 40);

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
                    text("GO!", Config.WIDTH / 2, Config.HEIGHT / 2);
                } else {
                    text(countdownValue, Config.WIDTH / 2, Config.HEIGHT / 2);
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
            visual.setActivePower(car.getActivePowerName()); // Poder ativo
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

    private void drawTrack() {
        // Fundo da pista
        fill(100);
        rect(50, Config.TRACK_Y_START - 20, Config.WIDTH - 100, Config.LANE_HEIGHT * carVisuals.size() + 40);

        // Linha de largada
        stroke(255);
        line(60, Config.TRACK_Y_START - 20, 60, Config.TRACK_Y_START + Config.LANE_HEIGHT * carVisuals.size() + 20);

        // Linha de chegada
        stroke(255);
        fill(255, 0, 0, 100);
        rect(Config.FINISH_LINE_X, Config.TRACK_Y_START - 20, 10, Config.LANE_HEIGHT * carVisuals.size() + 40);

        // Lanes
        noStroke();
        for (int i = 0; i < carVisuals.size(); i++) {
            if (i % 2 == 0) {
                fill(100);
            } else {
                fill(90);
            }
            rect(60, Config.TRACK_Y_START + i * Config.LANE_HEIGHT, Config.FINISH_LINE_X - 60, Config.LANE_HEIGHT);

            // Marcadores de distancia
            stroke(255, 255, 255, 100);
            for (int mark = 0; mark < 10; mark++) {
                float x = 60 + (Config.FINISH_LINE_X - 60) * mark / 10.0f;
                line(x, Config.TRACK_Y_START + i * Config.LANE_HEIGHT,
                        x, Config.TRACK_Y_START + (i + 1) * Config.LANE_HEIGHT);
            }
            noStroke();
        }
    }

    private void drawCars() {
        for (CarVisual carVisual : carVisuals.values()) {
            // Calcula X baseada na posição da corrida (0 a 100)
            float trackLength = Config.FINISH_LINE_X - 60;
            float xPos = 60 + min((float)(carVisual.displayPosition / 100.0 * trackLength), trackLength);
            float yPos = Config.TRACK_Y_START + carVisual.laneIndex * Config.LANE_HEIGHT + Config.LANE_HEIGHT / 2;

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

            if (carVisual.activePower != null && !carVisual.finished) {
                PImage powerImg;

                if (carVisual.activePower.equals("Banana")) {
                    powerImg = bananaImg;
                    fill(255, 255, 0);
                    text("Banana!", xPos, yPos - 25);
                } else if (carVisual.activePower.equals("Boost")) {
                    powerImg = boostImg;
                    fill(0, 0, 255);
                    text("Boost!", xPos, yPos - 25);
                } else if (carVisual.activePower.equals("RedShell")) {
                    powerImg = shellImg;
                    fill(255, 0, 0);
                    text("Red Shell!", xPos, yPos - 25);
                } else {
                    continue; // Poder desconhecido, não mostrar
                }

                image(powerImg, xPos - 25, yPos - 15);
            }
        }
    }

    private void drawResults() {
        // Configurações do painel de resultados
        int panelWidth = 400;
        int headerHeight = 50;
        int rowHeight = 30;
        int panelHeight = headerHeight + (finishEvents.size() * rowHeight) + 20;

        // Posiciona o painel no centro da tela
        int panelX = (Config.WIDTH - panelWidth) / 2;
        int panelY = (Config.HEIGHT - panelHeight) / 2;

        // Desenha o fundo do painel com borda
        fill(255, 255, 255, 230);
        stroke(50);
        strokeWeight(2);
        rect(panelX, panelY, panelWidth, panelHeight, 10);
        noStroke();

        // Cabeçalho com fundo destacado
        fill(30, 30, 120);
        rect(panelX, panelY, panelWidth, headerHeight, 10, 10, 0, 0);

        // Título "RESULTADOS"
        fill(255);
        textAlign(CENTER, CENTER);
        textSize(24);
        text("RESULTADOS", panelX + panelWidth/2, panelY + headerHeight/2);

        // Ordena os eventos por tempo de chegada
        List<RaceFinishEvent> sortedEvents = new ArrayList<>(finishEvents);
        sortedEvents.sort(Comparator.comparing(RaceFinishEvent::finishTime));

        // Variáveis para posicionamento das informações
        int startY = panelY + headerHeight + 15;
        int posX = panelX + 30;
        int nameX = panelX + 70;
        int timeX = panelX + panelWidth - 80;

        // Linha divisória abaixo dos cabeçalhos
        stroke(200);
        strokeWeight(1);
        line(panelX + 20, startY, panelX + panelWidth - 20, startY);
        noStroke();

        // Desenha cada linha de resultado
        for (int i = 0; i < sortedEvents.size(); i++) {
            RaceFinishEvent event = sortedEvents.get(i);
            Car car = event.car();

            // Posição Y para esta linha
            int yPos = startY + (i * rowHeight) + rowHeight/2;

            // Destaca a linha do 1º colocado
            if (i == 0) {
                fill(255, 250, 200, 100);
                rect(panelX + 10, yPos - rowHeight/2, panelWidth - 20, rowHeight, 5);
            }
            // Desenha linha com tom alternado para facilitar leitura
            else if (i % 2 == 1) {
                fill(240, 240, 240, 100);
                rect(panelX + 10, yPos - rowHeight/2, panelWidth - 20, rowHeight);
            }

            // Posição
            fill(i == 0 ? color(180, 150, 0) : (i == 1 ? color(120) : (i == 2 ? color(150, 90, 30) : color(50))));
            textAlign(CENTER, CENTER);
            textSize(16);
            text((i+1) + "º", posX, yPos);

            // Nome do carro
            fill(0);
            textAlign(LEFT, CENTER);
            textSize(14);
            text(car.getClass().getSimpleName() + " [" + car.threadId() + "]", nameX, yPos);

            // Tempo de chegada
            textAlign(RIGHT, CENTER);
            text(FormatEpochSecondToString.formatEpochSecond(event.getFinishTime()).substring(11), timeX, yPos);
        }
    }

    private void drawMessages() {
        // Mensagens da corrida no canto inferior esquerdo
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

    private class CarVisual {
        private String name;
        private long threadId;
        private PImage image;
        private int laneIndex;
        private double position = 0;
        private double speed;
        private boolean finished = false;
        private String activePower = null;
        private double targetPosition = 0;
        private double displayPosition = 0;
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

}
