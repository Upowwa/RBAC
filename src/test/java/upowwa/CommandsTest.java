package upowwa;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class CommandsTest {

    private RBACSystem system;
    private CommandParser parser;

    private PrintStream originalOut;
    private ByteArrayOutputStream output;

    @BeforeEach
    void setUp() {
        system = new RBACSystem();
        parser = new CommandParser();

        CommandRegistry.registerUserCommands(parser);

        originalOut = System.out;
        output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    private Scanner scannerOf(String input) {
        return new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
    }

    private String getOutput() {
        return output.toString(StandardCharsets.UTF_8);
    }

    private void execute(String command, String input) {
        parser.parseAndExecute(command, scannerOf(input), system);
    }

    @Test
    void userCreate_createsUser() {
        String input = """
            alice
            Alice Johnson
            alice@example.com
            """;

        execute("user-create", input);

        assertTrue(system.getUserManager().findByUsername("alice").isPresent());
        assertTrue(getOutput().toLowerCase().contains("успеш"));
    }

    @Test
    void userCreate_invalidEmail_showsError() {
        String input = """
                alice
                Alice Johnson
                wrong-email
                """;

        execute("user-create", input);

        assertTrue(system.getUserManager().findByUsername("alice").isEmpty());
        assertTrue(getOutput().toLowerCase().contains("ошибка"));
    }

    @Test
    void userDelete_removesUserAfterConfirmation() {
        system.getUserManager().add(User.create("alice", "Alice Johnson", "alice@example.com"));

        String input = """
                alice
                да
                """;

        execute("user-delete", input);

        assertTrue(system.getUserManager().findByUsername("alice").isEmpty());
    }

    @Test
    void userSearch_findsByUsername() {
        system.getUserManager().add(User.create("alice", "Alice Johnson", "alice@example.com"));

        String input = """
                1
                ali
                """;

        execute("user-search", input);

        assertTrue(getOutput().contains("alice"));
    }

    @Test
    void roleCreate_createsRole() {
        String input = """
                Admin
                Администратор
                нет
                """;

        execute("role-create", input);

        assertTrue(system.getRoleManager().findByName("Admin").isPresent());
    }

    @Test
    void roleDelete_removesRoleAfterConfirmation() {
        Role role = new Role("Admin", "Администратор");
        system.getRoleManager().add(role);

        String input = """
                Admin
                да
                """;

        execute("role-delete", input);

        assertTrue(system.getRoleManager().findByName("Admin").isEmpty());
    }

    @Test
    void assignRole_createsPermanentAssignment() {
        User user = User.create("alice", "Alice Johnson", "alice@example.com");
        Role role = new Role("Admin", "Администратор");

        system.getUserManager().add(user);
        system.getRoleManager().add(role);
        system.setCurrentUser("alice");

        String input = """
                alice
                1
                постоянное
                тестовая причина
                """;

        execute("assign-role", input);

        assertEquals(1, system.getAssignmentManager().findAll().size());
        assertTrue(getOutput().toLowerCase().contains("успеш"));
    }

    @Test
    void revokeRole_revokesPermanentAssignment() {
        User user = User.create("alice", "Alice Johnson", "alice@example.com");
        Role role = new Role("Admin", "Администратор");

        system.getUserManager().add(user);
        system.getRoleManager().add(role);

        PermanentAssignment assignment = new PermanentAssignment(
                user, role, AssignmentMetadata.now("system", "test")
        );
        system.getAssignmentManager().add(assignment);

        String input = """
                alice
                1
                """;

        execute("revoke-role", input);

        assertFalse(assignment.isActive());
    }

    @Test
    void assignmentExtend_extendsTemporaryAssignment() {
        User user = User.create("alice", "Alice Johnson", "alice@example.com");
        Role role = new Role("Viewer", "Просмотр");

        system.getUserManager().add(user);
        system.getRoleManager().add(role);

        TemporaryAssignment assignment = new TemporaryAssignment(
                user, role, AssignmentMetadata.now("system", "test"), "2030-01-01", false
        );
        system.getAssignmentManager().add(assignment);

        String input = """
                %s
                2031-01-01
                """.formatted(assignment.assignmentId());

        execute("assignment-extend", input);

        assertEquals("2031-01-01", assignment.getExpiresAt());
    }

    @Test
    void permissionsUser_printsGroupedPermissions() {
        User user = User.create("alice", "Alice Johnson", "alice@example.com");
        Role role = new Role("Admin", "Администратор");
        role.addPermission(new Permission("READ", "users", "Чтение"));
        role.addPermission(new Permission("WRITE", "users", "Запись"));
        role.addPermission(new Permission("READ", "roles", "Чтение ролей"));

        system.getUserManager().add(user);
        system.getRoleManager().add(role);
        system.getAssignmentManager().add(
                new PermanentAssignment(user, role, AssignmentMetadata.now("system", "test"))
        );

        execute("permissions-user", "alice\n");

        String out = getOutput();
        assertTrue(out.contains("users"));
        assertTrue(out.contains("roles"));
        assertTrue(out.contains("READ"));
    }

    @Test
    void permissionsCheck_showsPermissionAndRoleSource() {
        User user = User.create("alice", "Alice Johnson", "alice@example.com");
        Role role = new Role("Admin", "Администратор");
        role.addPermission(new Permission("READ", "users", "Чтение"));

        system.getUserManager().add(user);
        system.getRoleManager().add(role);
        system.getAssignmentManager().add(
                new PermanentAssignment(user, role, AssignmentMetadata.now("system", "test"))
        );

        String input = """
                alice
                READ
                users
                """;

        execute("permissions-check", input);

        String out = getOutput();
        assertTrue(out.toLowerCase().contains("имеет"));
        assertTrue(out.contains("Admin"));
    }

    @Test
    void help_printsCommands() {
        execute("help", "");

        String out = getOutput();
        assertTrue(out.contains("ДОСТУПНЫЕ КОМАНДЫ"));
    }

    @Test
    void stats_printsSystemStatistics() {
        execute("stats", "");

        String out = getOutput();
        assertTrue(out.contains("СТАТИСТИКА RBAC СИСТЕМЫ"));
        assertTrue(out.contains("Пользователей"));
        assertTrue(out.contains("Ролей"));
    }

    @Test
    void unknownCommand_printsError() {
        execute("abracadabra", "");

        String out = getOutput();
        assertTrue(out.contains("Неизвестная команда"));
    }
}