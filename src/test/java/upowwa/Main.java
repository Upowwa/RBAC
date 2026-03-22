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
    }
}
