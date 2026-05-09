package upowwa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

class ThreadsafeManagersTests {

    private UserManager userManager;
    private RoleManager roleManager;
    private AssignmentManager assignmentManager;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
        roleManager = new RoleManager();
        assignmentManager = new AssignmentManager(userManager, roleManager);
        roleManager.setAssignmentManager(assignmentManager);
    }

    @Test
    void userManager_addUser_success() {
        User user = User.create("alice", "Alice Smith", "alice@gmail.com");
        userManager.add(user);

        assertEquals(1, userManager.count());
        assertTrue(userManager.exists("alice"));
    }

    @Test
    void userManager_addDuplicateUser_throwsException() {
        User user = User.create("alice", "Alice Smith", "alice@gmail.com");
        userManager.add(user);

        assertThrows(IllegalArgumentException.class, () -> userManager.add(user));
    }

    @Test
    void userManager_findByUsername_returnsUser() {
        User user = User.create("alice", "Alice Smith", "alice@gmail.com");
        userManager.add(user);

        Optional<User> found = userManager.findByUsername("alice");
        assertTrue(found.isPresent());
        assertEquals("alice", found.get().username());
    }

    @Test
    void userManager_findByEmail_returnsUser() {
        User user = User.create("bob", "Bob Johnson", "bob@company.com");
        userManager.add(user);

        Optional<User> found = userManager.findByEmail("bob@company.com");
        assertTrue(found.isPresent());
        assertEquals("bob", found.get().username());
    }

    @Test
    void userManager_findByFilter_worksWithFilter() {
        userManager.add(User.create("alice", "Alice", "alice@gmail.com"));
        userManager.add(User.create("bob", "Bob", "bob@company.com"));

        List<User> gmailUsers = userManager.findByFilter(UserFilters.byEmailDomain("gmail.com"));
        assertEquals(1, gmailUsers.size());
        assertEquals("alice", gmailUsers.get(0).username());
    }

    @Test
    void userManager_findAll_withFilterAndSorter() {
        userManager.add(User.create("zebra", "Zebra User", "z@test.com"));
        userManager.add(User.create("apple", "Apple User", "a@test.com"));

        List<User> sorted = userManager.findAll(
                filter -> true,
                UserSorters.byUsername()
        );

        assertEquals(2, sorted.size());
        assertEquals("apple", sorted.get(0).username());
        assertEquals("zebra", sorted.get(1).username());
    }

    @Test
    void userManager_updateUser_success() {
        User user = User.create("alice", "Alice Smith", "alice@gmail.com");
        userManager.add(user);

        userManager.update("alice", "Alice New", "alice.new@gmail.com");

        Optional<User> updated = userManager.findByUsername("alice");
        assertTrue(updated.isPresent());
        assertEquals("Alice New", updated.get().fullName());
        assertEquals("alice.new@gmail.com", updated.get().email());
    }

    @Test
    void userManager_concurrentAdd_sameUsername_keepsOnlyOneUser() throws Exception {
        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);

        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            tasks.add(() -> {
                ready.countDown();
                start.await();
                try {
                    userManager.add(User.create("alice", "Alice Smith", "alice@gmail.com"));
                    return true;
                } catch (IllegalArgumentException e) {
                    return false;
                }
            });
        }

        List<Future<Boolean>> futures = new ArrayList<>();
        for (Callable<Boolean> task : tasks) {
            futures.add(executor.submit(task));
        }

        ready.await();
        start.countDown();

        int successCount = 0;
        for (Future<Boolean> future : futures) {
            if (future.get()) {
                successCount++;
            }
        }

        executor.shutdown();

        assertEquals(1, successCount);
        assertEquals(1, userManager.count());
        assertTrue(userManager.findByUsername("alice").isPresent());
    }

    @Test
    void roleManager_addRole_success() {
        Role role = new Role("Admin", "Full access");
        roleManager.add(role);

        assertEquals(1, roleManager.count());
        assertTrue(roleManager.findByName("Admin").isPresent());
    }

    @Test
    void roleManager_addDuplicateRole_throwsException() {
        Role role = new Role("Admin", "Full access");
        roleManager.add(role);

        assertThrows(IllegalArgumentException.class, () -> roleManager.add(new Role("Admin", "Other")));
    }

    @Test
    void roleManager_addPermissionToRole_success() {
        Role role = new Role("Admin", "Full access");
        Permission permission = new Permission("READ", "users", "Read users");
        roleManager.add(role);

        roleManager.addPermissionToRole("Admin", permission);

        Role stored = roleManager.findByName("Admin").orElseThrow();
        assertTrue(stored.hasPermission(permission));
    }

    @Test
    void roleManager_removePermissionFromRole_success() {
        Role role = new Role("Admin", "Full access");
        Permission permission = new Permission("READ", "users", "Read users");
        roleManager.add(role);
        roleManager.addPermissionToRole("Admin", permission);

        roleManager.removePermissionFromRole("Admin", permission);

        Role stored = roleManager.findByName("Admin").orElseThrow();
        assertFalse(stored.hasPermission(permission));
    }

    @Test
    void roleManager_findRolesWithPermission_returnsMatchingRoles() {
        Role admin = new Role("Admin", "Full access");
        Role viewer = new Role("Viewer", "Read only");
        Permission readUsers = new Permission("READ", "users", "Read users");

        roleManager.add(admin);
        roleManager.add(viewer);
        roleManager.addPermissionToRole("Admin", readUsers);
        roleManager.addPermissionToRole("Viewer", new Permission("READ", "reports", "Read reports"));

        List<Role> roles = roleManager.findRolesWithPermission("READ", "users");

        assertEquals(1, roles.size());
        assertEquals("Admin", roles.get(0).getName());
    }

    @Test
    void roleManager_concurrentAdd_sameRoleName_keepsOnlyOneRole() throws Exception {
        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);

        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            tasks.add(() -> {
                ready.countDown();
                start.await();
                try {
                    roleManager.add(new Role("Admin", "Full access"));
                    return true;
                } catch (IllegalArgumentException e) {
                    return false;
                }
            });
        }

        List<Future<Boolean>> futures = new ArrayList<>();
        for (Callable<Boolean> task : tasks) {
            futures.add(executor.submit(task));
        }

        ready.await();
        start.countDown();

        int successCount = 0;
        for (Future<Boolean> future : futures) {
            if (future.get()) {
                successCount++;
            }
        }

        executor.shutdown();

        assertEquals(1, successCount);
        assertEquals(1, roleManager.count());
        assertTrue(roleManager.findByName("Admin").isPresent());
    }

    @Test
    void assignmentManager_addAssignment_success() {
        User user = User.create("alice", "Alice Smith", "alice@gmail.com");
        Role role = new Role("Admin", "Full access");
        userManager.add(user);
        roleManager.add(role);

        AssignmentMetadata meta = AssignmentMetadata.now("system", "test");
        RoleAssignment assignment = new PermanentAssignment(user, role, meta);

        assignmentManager.add(assignment);

        assertEquals(1, assignmentManager.count());
        assertTrue(assignmentManager.findById(assignment.assignmentId()).isPresent());
    }

    @Test
    void assignmentManager_addAssignment_userDoesNotExist_throwsException() {
        Role role = new Role("Admin", "Full access");
        roleManager.add(role);

        AssignmentMetadata meta = AssignmentMetadata.now("system", "test");
        RoleAssignment assignment = new PermanentAssignment(User.create("alice", "Alice Smith", "alice@gmail.com"), role, meta);

        assertThrows(IllegalArgumentException.class, () -> assignmentManager.add(assignment));
    }

    @Test
    void assignmentManager_addAssignment_roleDoesNotExist_throwsException() {
        User user = User.create("alice", "Alice Smith", "alice@gmail.com");
        userManager.add(user);

        AssignmentMetadata meta = AssignmentMetadata.now("system", "test");
        RoleAssignment assignment = new PermanentAssignment(user, new Role("Admin", "Full access"), meta);

        assertThrows(IllegalArgumentException.class, () -> assignmentManager.add(assignment));
    }

    @Test
    void assignmentManager_addDuplicateActiveRole_throwsException() {
        User user = User.create("alice", "Alice Smith", "alice@gmail.com");
        Role role = new Role("Admin", "Full access");
        userManager.add(user);
        roleManager.add(role);

        AssignmentMetadata meta1 = AssignmentMetadata.now("system", "first");
        AssignmentMetadata meta2 = AssignmentMetadata.now("system", "second");

        assignmentManager.add(new PermanentAssignment(user, role, meta1));

        assertThrows(IllegalArgumentException.class,
                () -> assignmentManager.add(new PermanentAssignment(user, role, meta2)));
    }

    @Test
    void assignmentManager_revokeAssignment_deactivatesPermanentAssignment() {
        User user = User.create("alice", "Alice Smith", "alice@gmail.com");
        Role role = new Role("Admin", "Full access");
        userManager.add(user);
        roleManager.add(role);

        AssignmentMetadata meta = AssignmentMetadata.now("system", "test");
        PermanentAssignment assignment = new PermanentAssignment(user, role, meta);
        assignmentManager.add(assignment);

        assignmentManager.revokeAssignment(assignment.assignmentId());

        assertFalse(assignment.isActive());
        assertTrue(assignment.isRevoked());
    }

    @Test
    void assignmentManager_extendTemporaryAssignment_changesExpirationDate() {
        User user = User.create("alice", "Alice Smith", "alice@gmail.com");
        Role role = new Role("Admin", "Full access");
        userManager.add(user);
        roleManager.add(role);

        AssignmentMetadata meta = AssignmentMetadata.now("system", "test");
        TemporaryAssignment assignment = new TemporaryAssignment(
                user, role, meta, "2099-12-31", false
        );
        assignmentManager.add(assignment);

        assignmentManager.extendTemporaryAssignment(assignment.assignmentId(), "2100-01-31");

        assertEquals("2100-01-31", assignment.getExpiresAt());
    }

    @Test
    void assignmentManager_findByUser_returnsAssignments() {
        User user = User.create("alice", "Alice Smith", "alice@gmail.com");
        Role role = new Role("Admin", "Full access");
        userManager.add(user);
        roleManager.add(role);

        AssignmentMetadata meta = AssignmentMetadata.now("system", "test");
        RoleAssignment assignment = new PermanentAssignment(user, role, meta);
        assignmentManager.add(assignment);

        List<RoleAssignment> found = assignmentManager.findByUser(user);

        assertEquals(1, found.size());
        assertEquals(assignment.assignmentId(), found.get(0).assignmentId());
    }

    @Test
    void assignmentManager_findByRole_returnsAssignments() {
        User user = User.create("alice", "Alice Smith", "alice@gmail.com");
        Role role = new Role("Admin", "Full access");
        userManager.add(user);
        roleManager.add(role);

        AssignmentMetadata meta = AssignmentMetadata.now("system", "test");
        RoleAssignment assignment = new PermanentAssignment(user, role, meta);
        assignmentManager.add(assignment);

        List<RoleAssignment> found = assignmentManager.findByRole(role);

        assertEquals(1, found.size());
        assertEquals(assignment.assignmentId(), found.get(0).assignmentId());
    }

    @Test
    void assignmentManager_userHasRole_returnsTrue() {
        User user = User.create("alice", "Alice Smith", "alice@gmail.com");
        Role role = new Role("Admin", "Full access");
        userManager.add(user);
        roleManager.add(role);

        AssignmentMetadata meta = AssignmentMetadata.now("system", "test");
        assignmentManager.add(new PermanentAssignment(user, role, meta));

        assertTrue(assignmentManager.userHasRole(user, role));
    }

    @Test
    void assignmentManager_userHasPermission_returnsTrue() {
        User user = User.create("alice", "Alice Smith", "alice@gmail.com");
        Role role = new Role("Admin", "Full access");
        Permission permission = new Permission("READ", "users", "Read users");
        userManager.add(user);
        roleManager.add(role);
        roleManager.addPermissionToRole("Admin", permission);

        AssignmentMetadata meta = AssignmentMetadata.now("system", "test");
        assignmentManager.add(new PermanentAssignment(user, role, meta));

        assertTrue(assignmentManager.userHasPermission(user, "READ", "users"));
    }

    @Test
    void assignmentManager_getUserPermissions_returnsPermissions() {
        User user = User.create("alice", "Alice Smith", "alice@gmail.com");
        Role role = new Role("Admin", "Full access");
        Permission permission = new Permission("READ", "users", "Read users");
        userManager.add(user);
        roleManager.add(role);
        roleManager.addPermissionToRole("Admin", permission);

        AssignmentMetadata meta = AssignmentMetadata.now("system", "test");
        assignmentManager.add(new PermanentAssignment(user, role, meta));

        assertEquals(1, assignmentManager.getUserPermissions(user).size());
        assertTrue(assignmentManager.getUserPermissions(user).contains(permission));
    }

    @Test
    void assignmentManager_concurrentAdd_sameUserSameRole_keepsOnlyOneActiveAssignment() throws Exception {
        User user = User.create("alice", "Alice Smith", "alice@gmail.com");
        Role role = new Role("Admin", "Full access");
        userManager.add(user);
        roleManager.add(role);

        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);

        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            tasks.add(() -> {
                ready.countDown();
                start.await();
                try {
                    AssignmentMetadata meta = AssignmentMetadata.now("system", "parallel");
                    assignmentManager.add(new PermanentAssignment(user, role, meta));
                    return true;
                } catch (IllegalArgumentException e) {
                    return false;
                }
            });
        }

        List<Future<Boolean>> futures = new ArrayList<>();
        for (Callable<Boolean> task : tasks) {
            futures.add(executor.submit(task));
        }

        ready.await();
        start.countDown();

        int successCount = 0;
        for (Future<Boolean> future : futures) {
            if (future.get()) {
                successCount++;
            }
        }

        executor.shutdown();

        assertEquals(1, successCount);
        assertEquals(1, assignmentManager.count());
        assertTrue(assignmentManager.userHasRole(user, role));
    }

    @Test
    void userManager_findByFilterParallel_worksWithFilter() {
        userManager.add(User.create("alice", "Alice", "alice@gmail.com"));
        userManager.add(User.create("bob", "Bob", "bob@company.com"));

        List<User> gmailUsers = userManager.findByFilterParallel(UserFilters.byEmailDomain("gmail.com"));

        assertEquals(1, gmailUsers.size());
        assertEquals("alice", gmailUsers.get(0).username());
    }

    @Test
    void userManager_findByFilterParallel_nullFilter_throwsException() {
        assertThrows(NullPointerException.class, () -> userManager.findByFilterParallel(null));
    }

    @Test
    void roleManager_findByFilterParallel_worksWithFilter() {
        Role admin = new Role("Admin", "Full access");
        Role viewer = new Role("Viewer", "Read only");
        roleManager.add(admin);
        roleManager.add(viewer);

        List<Role> result = roleManager.findByFilterParallel(role -> role.getName().startsWith("A"));

        assertEquals(1, result.size());
        assertEquals("Admin", result.get(0).getName());
    }

    @Test
    void roleManager_findByFilterParallel_nullFilter_throwsException() {
        assertThrows(NullPointerException.class, () -> roleManager.findByFilterParallel(null));
    }

    @Test
    void assignmentManager_findByFilterParallel_worksWithFilter() {
        User user1 = User.create("alice", "Alice Smith", "alice@gmail.com");
        User user2 = User.create("bob", "Bob Smith", "bob@gmail.com");
        Role admin = new Role("Admin", "Full access");
        Role viewer = new Role("Viewer", "Read only");

        userManager.add(user1);
        userManager.add(user2);
        roleManager.add(admin);
        roleManager.add(viewer);

        AssignmentMetadata meta1 = AssignmentMetadata.now("system", "admin");
        AssignmentMetadata meta2 = AssignmentMetadata.now("system", "viewer");

        assignmentManager.add(new PermanentAssignment(user1, admin, meta1));
        assignmentManager.add(new PermanentAssignment(user2, viewer, meta2));

        List<RoleAssignment> result =
                assignmentManager.findByFilterParallel(a -> a.role().getName().equals("Admin"));

        assertEquals(1, result.size());
        assertEquals("alice", result.get(0).user().username());
        assertEquals("Admin", result.get(0).role().getName());
    }

    @Test
    void assignmentManager_findByFilterParallel_nullFilter_throwsException() {
        assertThrows(NullPointerException.class, () -> assignmentManager.findByFilterParallel(null));
    }
}