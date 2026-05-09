package upowwa;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScheduledTests {

    @Test
    void scheduledTask_shouldLogExpiredTemporaryAssignment() throws Exception {
        RBACSystem system = new RBACSystem();
        system.initialize();

        User user = User.create("tempuser", "Temp User", "temp@example.com");
        system.getUserManager().add(user);

        Role viewerRole = system.getRoleManager().findByName("Viewer").orElseThrow();

        AssignmentMetadata metadata = AssignmentMetadata.now("admin", "temporary access");
        TemporaryAssignment expiredAssignment = new TemporaryAssignment(
                user,
                viewerRole,
                metadata,
                "2020-01-01",
                false
        );

        system.getAssignmentManager().add(expiredAssignment);

        system.startScheduledTasks(1);

        waitForLogEntries(system.getAuditLog(), 2);

        List<AuditEntry> expiredLogs = system.getAuditLog().getByAction("TEMP_ASSIGNMENT_EXPIRED");
        assertFalse(expiredLogs.isEmpty());
        assertTrue(expiredLogs.stream()
                .anyMatch(entry -> entry.details().contains("Viewer")));

        system.shutdown();
    }

    @Test
    void scheduledTask_shouldLogSystemStatistics() throws Exception {
        RBACSystem system = new RBACSystem();
        system.initialize();

        system.startScheduledTasks(1);

        waitForAction(system.getAuditLog(), "SYSTEM_STATS", 1);

        List<AuditEntry> statsLogs = system.getAuditLog().getByAction("SYSTEM_STATS");
        assertFalse(statsLogs.isEmpty());
        assertTrue(statsLogs.get(0).details().contains("Пользователей"));
        assertTrue(statsLogs.get(0).details().contains("Ролей"));

        system.shutdown();
    }

    @Test
    void markExpiredTemporaryAssignments_shouldReturnExpiredOnlyOnce() {
        RBACSystem system = new RBACSystem();
        system.initialize();

        User user = User.create("tempuser2", "Temp User 2", "temp2@example.com");
        system.getUserManager().add(user);

        Role viewerRole = system.getRoleManager().findByName("Viewer").orElseThrow();

        AssignmentMetadata metadata = AssignmentMetadata.now("admin", "temporary access");
        TemporaryAssignment expiredAssignment = new TemporaryAssignment(
                user,
                viewerRole,
                metadata,
                "2020-01-01",
                false
        );

        system.getAssignmentManager().add(expiredAssignment);

        List<TemporaryAssignment> firstRun =
                system.getAssignmentManager().markExpiredTemporaryAssignments();
        List<TemporaryAssignment> secondRun =
                system.getAssignmentManager().markExpiredTemporaryAssignments();

        assertEquals(1, firstRun.size());
        assertEquals(0, secondRun.size());

        system.shutdown();
    }

    private void waitForLogEntries(AuditLog log, int minCount) throws InterruptedException {
        int attempts = 40;
        while (attempts-- > 0) {
            if (log.getAll().size() >= minCount) {
                return;
            }
            Thread.sleep(50);
        }
        fail("Недостаточно записей в AuditLog");
    }

    private void waitForAction(AuditLog log, String action, int minCount) throws InterruptedException {
        int attempts = 40;
        while (attempts-- > 0) {
            if (log.getByAction(action).size() >= minCount) {
                return;
            }
            Thread.sleep(50);
        }
        fail("Не дождались записи с действием: " + action);
    }
}