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
    }
}
