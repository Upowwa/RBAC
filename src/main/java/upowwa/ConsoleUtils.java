package upowwa;

import java.util.List;
import java.util.Scanner;

public class ConsoleUtils {

    public static String promptString(Scanner scanner, String message, boolean required) {
        while (true) {
            System.out.print(message + ": ");
            String input = scanner.nextLine().trim();

            if (!required || !input.isEmpty()) {
                return input;
            }

            System.out.println("✗ Поле обязательно для заполнения!");
        }
    }

    public static int promptInt(Scanner scanner, String message, int min, int max) {
        while (true) {
            System.out.print(message + " [" + min + "-" + max + "]: ");
            String input = scanner.nextLine().trim();

            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("✗ Введите число от " + min + " до " + max);
            } catch (NumberFormatException e) {
                System.out.println("✗ Введите корректное число!");
            }
        }
    }

    public static boolean promptYesNo(Scanner scanner, String message) {
        while (true) {
            System.out.print(message + " (y/n, да/нет): ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("y") || input.equals("yes") || input.equals("да")) {
                return true;
            }
            if (input.equals("n") || input.equals("no") || input.equals("нет")) {
                return false;
            }

            System.out.println("✗ Введите y/n или да/нет");
        }
    }

    public static <T> T promptChoice(Scanner scanner, String message, List<T> options) {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("Список вариантов не может быть пустым");
        }

        while (true) {
            System.out.println("\n" + "═".repeat(50));
            System.out.println(message);

            for (int i = 0; i < options.size(); i++) {
                System.out.printf("  %d. %s%n", i + 1, options.get(i));
            }

            System.out.println("\n" + "═".repeat(50));
            int choice = promptInt(scanner, "Выберите номер", 1, options.size());
            return options.get(choice - 1);
        }
    }
}