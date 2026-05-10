package com.example.notificationservice;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class NotificationWorkerManager {

    private final NotificationService notificationService;
    private ExecutorService executorService;
    private volatile boolean running = true;

    public NotificationWorkerManager(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostConstruct
    public void startWorkers() {
        int workers = 4;
        executorService = Executors.newFixedThreadPool(workers);

        for (int i = 1; i <= workers; i++) {
            int workerId = i;
            executorService.submit(() -> runWorker(workerId));
        }

        System.out.println("Started notification workers: 4");
    }

    private void runWorker(int workerId) {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                NotificationTask task = notificationService.claimNextTask();

                if (task == null) {
                    Thread.sleep(2000);
                    continue;
                }

                System.out.println("Worker-" + workerId + " processing task id=" + task.getId());

                try {
                    Thread.sleep(2000);
                    System.out.println("Notification sent: " + task.getMessage());
                    notificationService.markSent(task.getId());
                } catch (Exception ex) {
                    notificationService.markFailedOrRetry(task.getId());
                }

                Thread.sleep(300);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        System.out.println("Worker-" + workerId + " stopped");
    }

    @PreDestroy
    public void shutdown() {
        running = false;
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("Notification workers shutdown complete");
    }
}