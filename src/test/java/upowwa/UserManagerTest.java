package upowwa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserManagerTest {

    private UserManager userManager;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
    }

    @Test
    void addUser_success() {
        User user = User.create("alice", "Alice Smith", "alice@gmail.com");
        userManager.add(user);
        assertEquals(1, userManager.count());
        assertTrue(userManager.exists("alice"));
    }

    @Test
    void addDuplicateUser_throwsException() {
        User user = User.create("alice", "Alice Smith", "alice@gmail.com");
        userManager.add(user);

        assertThrows(IllegalArgumentException.class, () -> userManager.add(user));
    }

    @Test
    void findByUsername_returnsUser() {
        User user = User.create("alice", "Alice Smith", "alice@gmail.com");
        userManager.add(user);

        Optional<User> found = userManager.findByUsername("alice");
        assertTrue(found.isPresent());
        assertEquals("alice", found.get().username());
    }

    @Test
    void findByEmail_returnsUser() {
        User user = User.create("bob", "Bob Johnson", "bob@company.com");
        userManager.add(user);

        Optional<User> found = userManager.findByEmail("bob@company.com");
        assertTrue(found.isPresent());
        assertEquals("bob", found.get().username());
    }

    @Test
    void findByFilter_worksWithFilter() {
        userManager.add(User.create("alice", "Alice", "alice@gmail.com"));
        userManager.add(User.create("bob", "Bob", "bob@company.com"));

        List<User> gmailUsers = userManager.findByFilter(UserFilters.byEmailDomain("gmail.com"));
        assertEquals(1, gmailUsers.size());
        assertEquals("alice", gmailUsers.getFirst().username());
    }

    @Test
    void findAll_withFilterAndSorter() {
        userManager.add(User.create("zebra", "Zebra User", "z@test.com"));
        userManager.add(User.create("apple", "Apple User", "a@test.com"));

        List<User> sorted = userManager.findAll(
                filter -> true,
                UserSorters.byUsername()
        );
        assertEquals("apple", sorted.get(0).username());
        assertEquals("zebra", sorted.get(1).username());
    }

    @Test
    void updateUser_success() {
        User user = User.create("alice", "Alice Smith", "alice@gmail.com");
        userManager.add(user);

        userManager.update("alice", "Alice New", "alice.new@gmail.com");

        Optional<User> updated = userManager.findByUsername("alice");
        assertTrue(updated.isPresent());
        assertEquals("Alice New", updated.get().fullName());
        assertEquals("alice.new@gmail.com", updated.get().email());
    }
}
