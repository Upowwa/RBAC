package upowwa;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AuditLogTest {

    @Test
    void log_shouldAddEntry() {
        AuditLog log = new AuditLog();
        log.log("CREATE_USER", "admin", "john", "created user");

        assertEquals(1, log.getAll().size());
        assertEquals("CREATE_USER", log.getAll().get(0).action());
    }

    @Test
    void getByPerformer_shouldFilterEntries() {
        AuditLog log = new AuditLog();
        log.log("CREATE_USER", "admin", "john", "created");
        log.log("DELETE_ROLE", "manager", "analyst", "deleted");

        assertEquals(1, log.getByPerformer("admin").size());
    }

    @Test
    void getByAction_shouldFilterEntries() {
        AuditLog log = new AuditLog();
        log.log("CREATE_USER", "admin", "john", "created");
        log.log("CREATE_USER", "admin", "mary", "created");

        assertEquals(2, log.getByAction("CREATE_USER").size());
    }

    @Test
    void saveToFile_shouldWriteContent(@TempDir Path tempDir) throws Exception {
        AuditLog log = new AuditLog();
        log.log("CREATE_USER", "admin", "john", "created");

        Path file = tempDir.resolve("audit.txt");
        log.saveToFile(file.toString());

        String content = Files.readString(file);
        assertTrue(content.contains("CREATE_USER"));
        assertTrue(content.contains("admin"));
    }
}
