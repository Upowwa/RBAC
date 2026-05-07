package upowwa;

public class Main {
    public static void main(String[] args) {
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

        try {
            User.create(null, "Anastasia Fr", "Anastasia@gmail.com");
        } catch (IllegalArgumentException e) {
            System.out.println("Null username: " + e.getMessage());
        }

        try {
            User.create("anastasia_fr", "Anastasia Fr", "user.@gmail.com");
        } catch (IllegalArgumentException e) {
            System.out.println("Точка до @: " + e.getMessage());
        }

        System.out.println("___Тесты завершены___\n");

        System.out.println("\n___Тесты для Permission___");

        try {
            new Permission("READ WRITE", "users", "test");
        } catch (IllegalArgumentException e) {
            System.out.println("Пробел в name: " + e.getMessage());
        }

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

        System.out.println("___Тесты завершены___\n");

        System.out.println("\n___Тесты для Role___");

        Permission readUsers = new Permission("READ", "users", "Чтение пользователей");
        Permission writeUsers = new Permission("WRITE", "users", "Запись пользователей");
        Permission deleteReports = new Permission("DELETE", "reports", "Удаление отчетов");

        Role adminRole = new Role("Administrator", "Полный доступ к системе");
        adminRole.addPermission(readUsers);
        adminRole.addPermission(writeUsers);
        adminRole.addPermission(deleteReports);

        System.out.println("Создание роли: " + adminRole);
        System.out.println("\n" + adminRole.format());

        System.out.println("Есть READ users: " + adminRole.hasPermission(readUsers));
        System.out.println("Есть READ на users: " + adminRole.hasPermission("READ", "users"));
        System.out.println("Есть UNKNOWN: " + adminRole.hasPermission("UNKNOWN", "users"));

        adminRole.removePermission(writeUsers);
        System.out.println("\nПосле удаления WRITE: " + adminRole.getPermissions().size() + " прав");

        try {
            adminRole.addPermission(null);
        } catch (NullPointerException e) {
            System.out.println("Null permission: OK");
        }

        System.out.println("ID роли: " + adminRole.getId());

        try {
            adminRole.getPermissions().add(readUsers);
        } catch (UnsupportedOperationException e) {
            System.out.println("Коллекция permissions снаружи неизменяема: OK");
        }

        Role managerRole = new Role("Manager", "Ограниченный доступ");
        System.out.println("Уникальные ID: " + !adminRole.getId().equals(managerRole.getId()));

        System.out.println("___Тесты завершены___\n");

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

        System.out.println("assignmentId: " + perm.assignmentId());

        perm.revoke();
        System.out.println("После повторного revoke: " + perm.isActive());

        System.out.println("___Тесты завершены___\n");

        System.out.println("\n___Тесты для TemporaryAssignment___");

        TemporaryAssignment temp = new TemporaryAssignment(user, role, meta, "2026-12-31", false);

        System.out.println("Temporary - активное:");
        System.out.println("  summary(): " + temp.summary());
        System.out.println("  isActive(2026-05-01): " + temp.isActive("2026-05-01"));
        System.out.println("  isExpired(2027-01-01): " + temp.isExpired("2027-01-01"));

        temp.extend("2027-03-25");
        System.out.println("Temporary - после продления:");
        System.out.println("  expiresAt: " + temp.getExpiresAt());
        System.out.println("  isActive(2027-03-01): " + temp.isActive("2027-03-01"));
        System.out.println("  getTimeRemaining(): " + temp.getTimeRemaining());

        System.out.println("___Тесты завершены___\n");
    }
}
