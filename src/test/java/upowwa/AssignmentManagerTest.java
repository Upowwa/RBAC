package upowwa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Set;

class AssignmentManagerTest {

    private UserManager userMgr;
    private RoleManager roleMgr;
    private AssignmentManager assignmentMgr;

    @BeforeEach
    void setUp() {
        userMgr = new UserManager();
        roleMgr = new RoleManager();
        assignmentMgr = new AssignmentManager(userMgr, roleMgr);
    }

    @Test
    void addAssignment_success() {
        User alice = User.create("alice", "Alice", "a@test.com");
        Role admin = new Role("Admin", "Админ");
        userMgr.add(alice);
        roleMgr.add(admin);

        AssignmentMetadata meta = AssignmentMetadata.now("admin", "test");
        PermanentAssignment assignment = new PermanentAssignment(alice, admin, meta);
        assignmentMgr.add(assignment);

        assertEquals(1, assignmentMgr.count());
        assertTrue(assignmentMgr.userHasRole(alice, admin));
    }

    @Test
    void duplicateRoleForUser_throwsException() {
        User alice = User.create("alice", "Alice", "a@test.com");
        Role admin = new Role("Admin", "Админ");
        userMgr.add(alice);
        roleMgr.add(admin);

        AssignmentMetadata meta = AssignmentMetadata.now("admin", "test");
        PermanentAssignment first = new PermanentAssignment(alice, admin, meta);
        assignmentMgr.add(first);

        PermanentAssignment duplicate = new PermanentAssignment(alice, admin, meta);

        assertThrows(IllegalArgumentException.class, () -> {
            assignmentMgr.add(duplicate);
        });
    }

    @Test
    void userHasRole_returnsFalse_forInactive() {
        User alice = User.create("alice", "Alice", "a@test.com");
        Role admin = new Role("Admin", "Админ");
        userMgr.add(alice);
        roleMgr.add(admin);

        AssignmentMetadata meta = AssignmentMetadata.now("admin", "test");
        PermanentAssignment assignment = new PermanentAssignment(alice, admin, meta);
        assignmentMgr.add(assignment);

        assignmentMgr.revokeAssignment(assignment.assignmentId());

        assertFalse(assignmentMgr.userHasRole(alice, admin));
    }

    @Test
    void getUserPermissions_returnsAllPermissions() {
        User alice = User.create("alice", "Alice", "a@test.com");
        Role admin = new Role("Admin", "Полный доступ");
        admin.addPermission(new Permission("READ", "users", "Чтение"));
        admin.addPermission(new Permission("WRITE", "reports", "Запись"));

        userMgr.add(alice);
        roleMgr.add(admin);

        AssignmentMetadata meta = AssignmentMetadata.now("admin", "test");
        PermanentAssignment assignment = new PermanentAssignment(alice, admin, meta);
        assignmentMgr.add(assignment);

        Set<Permission> permissions = assignmentMgr.getUserPermissions(alice);
        assertEquals(2, permissions.size());
    }
}

