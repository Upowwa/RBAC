package upowwa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentManagerTest {

    private UserManager userMgr;
    private RoleManager roleMgr;
    private AssignmentManager assignmentMgr;

    private User alice;
    private User bob;
    private Role admin;
    private Role manager;

    @BeforeEach
    void setUp() {
        userMgr = new UserManager();
        roleMgr = new RoleManager();
        assignmentMgr = new AssignmentManager(userMgr, roleMgr);
        roleMgr.setAssignmentManager(assignmentMgr);

        alice = User.create("alice", "Alice Johnson", "alice@test.com");
        bob = User.create("bob", "Bob Smith", "bob@test.com");

        admin = new Role("Admin", "Администратор");
        admin.addPermission(new Permission("READ", "users", "Чтение пользователей"));
        admin.addPermission(new Permission("WRITE", "reports", "Запись отчетов"));

        manager = new Role("Manager", "Менеджер");
        manager.addPermission(new Permission("READ", "reports", "Чтение отчетов"));

        userMgr.add(alice);
        userMgr.add(bob);
        roleMgr.add(admin);
        roleMgr.add(manager);
    }

    @Test
    void addAssignment_success() {
        AssignmentMetadata meta = AssignmentMetadata.now("system", "test");
        PermanentAssignment assignment = new PermanentAssignment(alice, admin, meta);

        assignmentMgr.add(assignment);

        assertEquals(1, assignmentMgr.count());
        assertTrue(assignmentMgr.findById(assignment.assignmentId()).isPresent());
        assertTrue(assignmentMgr.userHasRole(alice, admin));
    }

    @Test
    void addAssignment_userDoesNotExist_throwsException() {
        User ghost = User.create("ghost", "Ghost User", "ghost@test.com");
        AssignmentMetadata meta = AssignmentMetadata.now("system", "test");
        PermanentAssignment assignment = new PermanentAssignment(ghost, admin, meta);

        assertThrows(IllegalArgumentException.class, () -> assignmentMgr.add(assignment));
    }

    @Test
    void addAssignment_roleDoesNotExist_throwsException() {
        Role unknownRole = new Role("Unknown", "Неизвестная роль");
        AssignmentMetadata meta = AssignmentMetadata.now("system", "test");
        PermanentAssignment assignment = new PermanentAssignment(alice, unknownRole, meta);

        assertThrows(IllegalArgumentException.class, () -> assignmentMgr.add(assignment));
    }

    @Test
    void addAssignment_duplicateActiveRoleForUser_throwsException() {
        AssignmentMetadata meta = AssignmentMetadata.now("system", "test");

        PermanentAssignment first = new PermanentAssignment(alice, admin, meta);
        PermanentAssignment duplicate = new PermanentAssignment(alice, admin, meta);

        assignmentMgr.add(first);

        assertThrows(IllegalArgumentException.class, () -> assignmentMgr.add(duplicate));
    }

    @Test
    void findByUser_returnsOnlyUserAssignments() {
        AssignmentMetadata meta = AssignmentMetadata.now("system", "test");

        assignmentMgr.add(new PermanentAssignment(alice, admin, meta));
        assignmentMgr.add(new PermanentAssignment(bob, manager, meta));

        List<RoleAssignment> aliceAssignments = assignmentMgr.findByUser(alice);

        assertEquals(1, aliceAssignments.size());
        assertEquals(alice, aliceAssignments.get(0).user());
    }

    @Test
    void findByRole_returnsOnlyRoleAssignments() {
        AssignmentMetadata meta = AssignmentMetadata.now("system", "test");

        assignmentMgr.add(new PermanentAssignment(alice, admin, meta));
        assignmentMgr.add(new PermanentAssignment(bob, manager, meta));

        List<RoleAssignment> adminAssignments = assignmentMgr.findByRole(admin);

        assertEquals(1, adminAssignments.size());
        assertEquals(admin, adminAssignments.get(0).role());
    }

    @Test
    void userHasRole_returnsFalse_whenAssignmentRevoked() {
        AssignmentMetadata meta = AssignmentMetadata.now("system", "test");
        PermanentAssignment assignment = new PermanentAssignment(alice, admin, meta);

        assignmentMgr.add(assignment);
        assignmentMgr.revokeAssignment(assignment.assignmentId());

        assertFalse(assignmentMgr.userHasRole(alice, admin));
    }

    @Test
    void userHasPermission_returnsTrue_whenPermissionExists() {
        AssignmentMetadata meta = AssignmentMetadata.now("system", "test");
        assignmentMgr.add(new PermanentAssignment(alice, admin, meta));

        assertTrue(assignmentMgr.userHasPermission(alice, "READ", "users"));
        assertTrue(assignmentMgr.userHasPermission(alice, "WRITE", "reports"));
        assertFalse(assignmentMgr.userHasPermission(alice, "DELETE", "users"));
    }

    @Test
    void getUserPermissions_returnsAllPermissionsFromAllRoles() {
        AssignmentMetadata meta = AssignmentMetadata.now("system", "test");

        assignmentMgr.add(new PermanentAssignment(alice, admin, meta));
        assignmentMgr.add(new PermanentAssignment(alice, manager, meta));

        Set<Permission> permissions = assignmentMgr.getUserPermissions(alice);

        assertEquals(3, permissions.size());
        assertTrue(permissions.contains(new Permission("READ", "users", "Чтение пользователей")));
        assertTrue(permissions.contains(new Permission("WRITE", "reports", "Запись отчетов")));
        assertTrue(permissions.contains(new Permission("READ", "reports", "Чтение отчетов")));
    }

    @Test
    void revokeAssignment_nonExisting_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> assignmentMgr.revokeAssignment("unknown-id"));
    }

    @Test
    void extendTemporaryAssignment_success() {
        AssignmentMetadata meta = AssignmentMetadata.now("system", "test");
        TemporaryAssignment assignment =
                new TemporaryAssignment(alice, admin, meta, "2099-12-31", false);

        assignmentMgr.add(assignment);
        assignmentMgr.extendTemporaryAssignment(assignment.assignmentId(), "2100-01-31");

        assertEquals("2100-01-31", assignment.getExpiresAt());
    }

    @Test
    void extendTemporaryAssignment_forPermanent_throwsException() {
        AssignmentMetadata meta = AssignmentMetadata.now("system", "test");
        PermanentAssignment assignment = new PermanentAssignment(alice, admin, meta);

        assignmentMgr.add(assignment);

        assertThrows(IllegalArgumentException.class,
                () -> assignmentMgr.extendTemporaryAssignment(assignment.assignmentId(), "2100-01-31"));
    }

    @Test
    void getActiveAssignments_returnsOnlyActive() {
        AssignmentMetadata meta = AssignmentMetadata.now("system", "test");

        PermanentAssignment active = new PermanentAssignment(alice, admin, meta);
        PermanentAssignment revoked = new PermanentAssignment(bob, manager, meta);

        assignmentMgr.add(active);
        assignmentMgr.add(revoked);
        assignmentMgr.revokeAssignment(revoked.assignmentId());

        List<RoleAssignment> activeAssignments = assignmentMgr.getActiveAssignments();

        assertEquals(1, activeAssignments.size());
        assertEquals(active.assignmentId(), activeAssignments.get(0).assignmentId());
    }

    @Test
    void removeAssignment_success() {
        AssignmentMetadata meta = AssignmentMetadata.now("system", "test");
        PermanentAssignment assignment = new PermanentAssignment(alice, admin, meta);

        assignmentMgr.add(assignment);
        boolean removed = assignmentMgr.remove(assignment);

        assertTrue(removed);
        assertEquals(0, assignmentMgr.count());
    }
}