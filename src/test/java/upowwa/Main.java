package upowwa;

public class Main {
    static void main() {
        System.out.println("___Тесты для User___");

        try {
            User user1 = User.create("anastasia_fr", "Anastasia Fr", "Anastasia@gmail.com");
            System.out.println("OK: " + user1.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }

        try {
            User.create("ab", "Anastasia Fr", "Anastasia@gmail.com");
        } catch (IllegalArgumentException e) {
            System.out.println("Username < 3: " + e.getMessage());
        }

        try {
            User.create("a".repeat(21), "Anastasia Fr", "Anastasia@gmail.com");
        } catch (IllegalArgumentException e) {
            System.out.println("Username > 20: " + e.getMessage());
        }

        try {
            User.create("anastasia-фr", "Anastasia Fr", "Anastasia@gmail.com");
        } catch (IllegalArgumentException e) {
            System.out.println("Неверные символы: " + e.getMessage());
        }

        try {
            User.create("anastasia_fr", "Anastasia Fr", "Anastasia.gmail.com");
        } catch (IllegalArgumentException e) {
            System.out.println("Email без @: " + e.getMessage());
        }

        try {
            User.create("", "Anastasia Fr", "Anastasia@gmail.com");
        } catch (IllegalArgumentException e) {
            System.out.println("Пустой username: " + e.getMessage());
        }

        System.out.println("\n___Тесты для Permission___");

        try {
            Permission perm1 = new Permission("read", "Users", "Чтение пользователей");
            System.out.println("OK: " + perm1.format());
            System.out.println("Поиск READ/users: " + perm1.matches("READ", "users"));
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка Permission: " + e.getMessage());
        }

        try {
            Permission perm2 = new Permission("Write", "reports", "Запись отчетов");
            System.out.println("Нормализация регистр: " + perm2.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }

        try {
            new Permission("READ", "", "test");
        } catch (IllegalArgumentException e) {
            System.out.println("Пустой resource: " + e.getMessage());
        }

        try {
            new Permission("", "users", "test");
        } catch (IllegalArgumentException e) {
            System.out.println("Пустой name: " + e.getMessage());
        }

        try {
            new Permission(null, "users", "test");
        } catch (IllegalArgumentException e) {
            System.out.println("Null name: " + e.getMessage());
        }

        try {
            new Permission("read", "users", "");
        } catch (IllegalArgumentException e) {
            System.out.println("Пустой description: " + e.getMessage());
        }

        System.out.println("\n___Тесты для Role___");

        //создание права
        Permission readUsers = new Permission("READ", "users", "Чтение пользователей");
        Permission writeUsers = new Permission("WRITE", "users", "Запись пользователей");
        Permission deleteReports = new Permission("DELETE", "reports", "Удаление отчетов");

        //создание роли
        Role adminRole = new Role("Administrator", "Полный доступ к системе");
        adminRole.addPermission(readUsers);
        adminRole.addPermission(writeUsers);
        adminRole.addPermission(deleteReports);

        System.out.println("Создание роли: " + adminRole);
        System.out.println("\n" + adminRole.format());

        System.out.println("Есть READ users: " + adminRole.hasPermission(readUsers));
        System.out.println("Есть READ на users: " + adminRole.hasPermission("READ", "users"));
        System.out.println("Есть UNKNOWN: " + adminRole.hasPermission("UNKNOWN", "users"));

        //удаление
        adminRole.removePermission(writeUsers);
        System.out.println("\nПосле удаления WRITE: " + adminRole.getPermissions().size() + " прав");

        try {
            adminRole.addPermission(null);
        } catch (NullPointerException e) {
            System.out.println("Null permission: OK");
        }

        System.out.println("\n___Тесты для AssignmentMetadata___");

        //тест с причиной
        AssignmentMetadata meta1 = AssignmentMetadata.now("anastasia_fr", "Тестирование системы");
        System.out.println("С причиной: " + meta1.format());

        //тест без причины (null)
        AssignmentMetadata meta2 = AssignmentMetadata.now("admin", null);
        System.out.println("Без причины: " + meta2.format());

        System.out.println("\n___Тест для RoleAssignment___");
        System.out.println("Интерфейс создан");

        System.out.println("\n___Тесты для AbstractRoleAssignment___");

        //создание объектов
        User testUser = User.create("testuser", "Test User", "test@example.com");
        Role testRole = new Role("TestRole", "Тестовая роль");
        AssignmentMetadata testMeta = AssignmentMetadata.now("admin", "Тестовое назначение");

        System.out.println("assignmentId: assign_1, assign_2...");
        System.out.println("Пример: " +
                String.format("[PERMANENT] %s assigned to %s by %s at 2026-02-07 19:00 \nReason: Initial setup \nStatus: ACTIVE",
                        testRole.getName(), testUser.username(), testMeta.assignedBy()));

        System.out.println("\n___Тесты для PermanentAssignment___");

        User user = User.create("testuser", "Test User", "test@example.com");
        Role role = new Role("TestRole", "Тестовая роль");
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Initial setup");

        PermanentAssignment perm = new PermanentAssignment(user, role, meta);

        System.out.println("До отзыва: " + perm.summary());
        System.out.println("isActive(): " + perm.isActive());
        System.out.println("assignmentType(): " + perm.assignmentType());

        perm.revoke();
        System.out.println("После отзыва: isActive(): " + perm.isActive());
        System.out.println("isRevoked(): " + perm.isRevoked());

        System.out.println("\n___Тесты для TemporaryAssignment___");

        TemporaryAssignment temp = new TemporaryAssignment(testUser, testRole, testMeta, "2026-12-31", false);
        System.out.println("Temporary - активное:");
        System.out.println("  summary(): " + temp.summary());
        System.out.println("  isActive(): " + temp.isActive());
        System.out.println("  isExpired(): " + temp.isExpired());

        temp.extend("2026-03-25");
        System.out.println("Temporary - после продления: " + temp.summary());
        System.out.println("  getTimeRemaining(): " + temp.getTimeRemaining());
    }
}
