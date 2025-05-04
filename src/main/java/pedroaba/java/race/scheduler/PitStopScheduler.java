package pedroaba.java.race.scheduler;

import pedroaba.java.race.constants.FeatureFlags;
import pedroaba.java.race.entities.Car;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class PitStopScheduler {
    private final List<Car> queue = new ArrayList<>();
    private final Semaphore exitSemaphore = new Semaphore(0);
    private final SchedulingAlgorithm<Car> algorithm;
    private volatile boolean running = true;

    public PitStopScheduler(SchedulingAlgorithm<Car> algorithm) {
        this.algorithm = algorithm;
        new Thread(this::scheduleLoop, "PitStop-Scheduler").start();
    }

    // chamado por cada Car assim que chega no pit-stop
    public synchronized void register(Car car) {
        queue.add(car);
        notify(); // acorda scheduleLoop se estiver dormindo
    }

    // chamado pelo Car quando sai do pit-stop
    public void notifyExit() {
        exitSemaphore.release();
    }

    private void scheduleLoop() {
        while (running) {
            Car next;
            synchronized (this) {
                while (queue.isEmpty()) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                // snapshot + escolhe segundo o algoritmo
                next = algorithm.next(new ArrayList<>(queue));
                queue.remove(next);
            }

            // libera o carro escolhido
            if (FeatureFlags.applyFCFSSchedulingAlgorithm) {
                next.grantPermit();
            }

            // aguarda até que ele sinalize saída
            try {
                exitSemaphore.acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    public void shutdown() {
        running = false;
        // opcional: notificar para sair imediatamente
        synchronized (this) { notifyAll(); }
    }
}
