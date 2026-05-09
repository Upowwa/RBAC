package upowwa;

import org.junit.jupiter.api.Test;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class WorkersTests {

    @Test
    void backgroundExecutor_shouldExecuteRunnableTask() throws Exception {
        BackgroundExecutor executor = new BackgroundExecutor();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger value = new AtomicInteger(0);

        Future<?> future = executor.submit(() -> {
            value.set(42);
            latch.countDown();
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        future.get();
        assertEquals(42, value.get());

        executor.shutdown();
    }

    @Test
    void backgroundExecutor_shouldExecuteCallableTask() throws Exception {
        BackgroundExecutor executor = new BackgroundExecutor();

        Future<String> future = executor.submit(() -> "done");

        assertEquals("done", future.get(2, TimeUnit.SECONDS));

        executor.shutdown();
    }

    @Test
    void backgroundExecutor_shouldThrowExceptionForNullRunnable() {
        BackgroundExecutor executor = new BackgroundExecutor();

        assertThrows(IllegalArgumentException.class, () -> executor.submit((Runnable) null));

        executor.shutdown();
    }

    @Test
    void backgroundExecutor_shouldThrowExceptionForInvalidThreadCount() {
        assertThrows(IllegalArgumentException.class, () -> new BackgroundExecutor(0));
    }

    @Test
    void backgroundExecutor_shouldShutdownCorrectly() {
        BackgroundExecutor executor = new BackgroundExecutor();

        executor.shutdown();

        assertTrue(executor.isShutdown());
    }

    @Test
    void rbacSystem_shouldCreateBackgroundExecutor() {
        RBACSystem system = new RBACSystem();

        assertNotNull(system.getBackgroundExecutor());

        system.shutdown();
    }

    @Test
    void rbacSystem_shouldRunTaskInBackgroundExecutor() throws Exception {
        RBACSystem system = new RBACSystem();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger result = new AtomicInteger(0);

        Future<?> future = system.getBackgroundExecutor().submit(() -> {
            result.set(100);
            latch.countDown();
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        future.get();
        assertEquals(100, result.get());

        system.shutdown();
    }

    @Test
    void reportUsersAsync_shouldGenerateReportInBackground() throws Exception {
        RBACSystem system = new RBACSystem();
        system.initialize();

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> reportRef = new AtomicReference<>();

        Future<?> future = system.getBackgroundExecutor().submit(() -> {
            String report = system.getReportGenerator()
                    .generateUserReport(system.getUserManager(), system.getAssignmentManager());
            reportRef.set(report);
            latch.countDown();
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        future.get();

        String report = reportRef.get();
        assertNotNull(report);
        assertTrue(report.contains("ОТЧЁТ ПО ПОЛЬЗОВАТЕЛЯМ"));
        assertTrue(report.contains("admin"));

        system.shutdown();
    }

    @Test
    void saveAsync_shouldSaveReportToFileInBackground() throws Exception {
        RBACSystem system = new RBACSystem();
        system.initialize();

        File tempFile = File.createTempFile("rbac-report-", ".txt");
        tempFile.deleteOnExit();

        CountDownLatch latch = new CountDownLatch(1);

        Future<?> future = system.getBackgroundExecutor().submit(() -> {
            try {
                String report = system.getReportGenerator()
                        .generateUserReport(system.getUserManager(), system.getAssignmentManager());
                system.getReportGenerator().exportToFile(report, tempFile.getAbsolutePath());
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        future.get();

        assertTrue(tempFile.exists());
        String content = java.nio.file.Files.readString(tempFile.toPath());
        assertNotNull(content);
        assertTrue(content.contains("ОТЧЁТ ПО ПОЛЬЗОВАТЕЛЯМ"));
        assertTrue(content.contains("admin"));

        system.shutdown();
    }

    @Test
    void stressTest_concurrentOperations_shouldNotBreakSystem() throws Exception {
        RBACSystem system = new RBACSystem();
        system.initialize();

        int threads = 8;
        int iterations = 20;

        java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(threads);
        java.util.concurrent.CountDownLatch ready = new java.util.concurrent.CountDownLatch(threads);
        java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            final int index = i;
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();

                    for (int j = 0; j < iterations; j++) {
                        String username = "stress_user_" + index + "_" + j;

                        if (!system.getUserManager().exists(username)) {
                            system.getUserManager().add(User.create(
                                    username,
                                    "User " + index + " " + j,
                                    username + "@mail.com"
                            ));
                        }

                        system.getUserManager().findByUsername(username).ifPresent(user -> {
                            system.getUserManager().findAll();
                            system.getRoleManager().findAll();
                            system.getAssignmentManager().findByUser(user);
                        });
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        assertTrue(ready.await(5, TimeUnit.SECONDS));
        start.countDown();
        assertTrue(done.await(10, TimeUnit.SECONDS));

        pool.shutdownNow();

        assertTrue(system.getUserManager().count() >= 1);
        assertTrue(system.getRoleManager().count() >= 3);
        assertTrue(system.getAssignmentManager().count() >= 1);
        system.shutdown();
    }


}