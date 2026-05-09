package upowwa;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AuditLogTest {

    @Test
    void log_shouldAddEntry() throws Exception {
        AuditLog log = new AuditLog();
        log.log("CREATE_USER", "admin", "john", "created user");

        waitForEntries(log, 1);

        assertEquals(1, log.getAll().size());
        assertEquals("CREATE_USER", log.getAll().get(0).action());

        log.shutdown();
    }

    @Test
    void getByPerformer_shouldFilterEntries() throws Exception {
        AuditLog log = new AuditLog();
        log.log("CREATE_USER", "admin", "john", "created");
        log.log("DELETE_ROLE", "manager", "analyst", "deleted");

        waitForEntries(log, 2);

        assertEquals(1, log.getByPerformer("admin").size());

        log.shutdown();
    }

    @Test
    void getByAction_shouldFilterEntries() throws Exception {
        AuditLog log = new AuditLog();
        log.log("CREATE_USER", "admin", "john", "created");
        log.log("CREATE_USER", "admin", "mary", "created");

        waitForEntries(log, 2);

        assertEquals(2, log.getByAction("CREATE_USER").size());

        log.shutdown();
    }

    @Test
    void saveToFile_shouldWriteContent(@TempDir Path tempDir) throws Exception {
        AuditLog log = new AuditLog();
        log.log("CREATE_USER", "admin", "john", "created");

        waitForEntries(log, 1);

        Path file = tempDir.resolve("audit.txt");
        log.saveToFile(file.toString());

        String content = Files.readString(file);
        assertTrue(content.contains("CREATE_USER"));
        assertTrue(content.contains("admin"));

        log.shutdown();
    }

    @Test
    void log_shouldHandleConcurrentWrites() throws Exception {
        AuditLog log = new AuditLog();

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                log.log("ACTION1", "user1", "target" + i, "details");
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                log.log("ACTION2", "user2", "target" + i, "details");
            }
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        waitForEntries(log, 20);

        assertEquals(20, log.getAll().size());

        log.shutdown();
    }

    private void waitForEntries(AuditLog log, int expectedCount) throws InterruptedException {
        int attempts = 20;
        while (attempts-- > 0) {
            if (log.getAll().size() >= expectedCount) {
                return;
            }
            Thread.sleep(50);
        }
        fail("Записи в AuditLog не появились вовремя");
    }
}