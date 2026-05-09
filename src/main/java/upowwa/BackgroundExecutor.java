package upowwa;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class BackgroundExecutor {
    private final ExecutorService executorService;

    public BackgroundExecutor() {
        this(2);
    }

    public BackgroundExecutor(int threadCount) {
        if (threadCount <= 0) {
            throw new IllegalArgumentException("Количество потоков должно быть больше 0");
        }
        this.executorService = Executors.newFixedThreadPool(threadCount);
    }

    public Future<?> submit(Runnable task) {
        if (task == null) {
            throw new IllegalArgumentException("Задача не может быть null");
        }
        return executorService.submit(task);
    }

    public <T> Future<T> submit(Callable<T> task) {
        if (task == null) {
            throw new IllegalArgumentException("Задача не может быть null");
        }
        return executorService.submit(task);
    }

    public void execute(Runnable task) {
        if (task == null) {
            throw new IllegalArgumentException("Задача не может быть null");
        }
        executorService.execute(task);
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public boolean isShutdown() {
        return executorService.isShutdown();
    }

    public boolean isTerminated() {
        return executorService.isTerminated();
    }
}