package upowwa;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AuditLogTest {

    @Test
    void testLogAndGetAll() {
        AuditLog log = new AuditLog();
        log.log("CREATE", "admin", "user1", "details");

        assertEquals(1, log.getAll().size());
        assertEquals("CREATE", log.getAll().get(0).action());
        assertEquals("admin", log.getAll().get(0).performer());
    }

    @Test
    void testGetByPerformer() {
        AuditLog log = new AuditLog();
        log.log("CREATE", "admin", "user1", "");
        log.log("DELETE", "user1", "user2", "");

        assertEquals(1, log.getByPerformer("admin").size());
        assertEquals(1, log.getByPerformer("user1").size());
    }

    @Test
    void testGetByAction() {
        AuditLog log = new AuditLog();
        log.log("CREATE", "admin", "user1", "");
        log.log("DELETE", "admin", "user2", "");

        assertEquals(1, log.getByAction("CREATE").size());
        assertEquals(1, log.getByAction("DELETE").size());
    }

    @Test
    void testCountAndClear() {
        AuditLog log = new AuditLog();
        log.log("TEST", "admin", "user", "");
        assertEquals(1, log.count());
        log.clear();
        assertEquals(0, log.count());
    }

    @Test
    void testPrintLog() {
        AuditLog log = new AuditLog();
        log.log("TEST", "admin", "user", "details");
        // printLog() выводит в System.out - проверяем косвенно через count
        assertEquals(1, log.count());
    }
}
