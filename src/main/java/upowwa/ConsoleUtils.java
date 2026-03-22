package upowwa;
//нужно доделать
import java.util.List;
import java.util.Scanner;
import java.util.stream.IntStream;

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
            try {
                System.out.print(message + " [" + min + "-" + max + "]: ");
                String input = scanner.nextLine().trim();
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
            System.out.print(message + " (y/n): ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("y") || input.equals("yes") || input.equals("да")) {
                return true;
            } else if (input.equals("n") || input.equals("no") || input.equals("нет")) {
                return false;
            }
            System.out.println("✗ Введите y/n или да/нет");
        }
    }

    public static int promptChoice(Scanner scanner, String message, List<String> options) {
        while (true) {
            System.out.println("\n" + "═".repeat(50));
            System.out.println(message);
            IntStream.range(0, options.size()).forEach(i ->
                    System.out.printf("  %d. %s\n", i + 1, options.get(i)));
            System.out.println("═".repeat(50));

            try {
                int choice = ConsoleUtils.promptInt(scanner, "Выберите номер", 1, options.size());
                return choice - 1; // вернуть индекс (0-based)
            } catch (Exception e) {
                System.out.println("✗ Неверный выбор!");
            }
        }
    }
}
