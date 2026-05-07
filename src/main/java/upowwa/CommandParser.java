package upowwa;

import java.util.*;
import java.util.Scanner;

public class CommandParser {
    private final Map<String, Command> commands;
    private final Map<String, String> commandDescriptions;

    public CommandParser() {
        this.commands = new HashMap<>();
        this.commandDescriptions = new HashMap<>();
    }

    public void registerCommand(String name, String description, Command command) {
        Objects.requireNonNull(name, "Имя команды не может быть null");
        Objects.requireNonNull(description, "Описание команды не может быть null");
        Objects.requireNonNull(command, "Command не может быть null");

        String key = name.trim().toLowerCase();
        commands.put(key, command);
        commandDescriptions.put(key, description);
    }

    public void executeCommand(String commandName, String arguments, Scanner scanner, RBACSystem system) {
        String cmdName = commandName.toLowerCase();
        Command cmd = commands.get(cmdName);

        if (cmd != null) {
            cmd.execute(arguments, scanner, system);
        } else {
            System.out.println("Команда '" + commandName + "' не найдена!");
        }
    }

    public void printHelp() {
        System.out.println("\nДОСТУПНЫЕ КОМАНДЫ:");

        List<String> sortedCommands = new ArrayList<>(commands.keySet());
        sortedCommands.sort(String::compareTo);

        for (String cmd : sortedCommands) {
            System.out.printf("%-12s — %s\n", cmd, commandDescriptions.get(cmd));
        }
    }

    public void parseAndExecute(String input, Scanner scanner, RBACSystem system) {
        if (input == null || input.trim().isEmpty()) {
            System.out.println("Введите команду");
            return;
        }

        String[] parts = input.trim().split("\\s+", 2);
        String commandName = parts[0].toLowerCase();
        String arguments = parts.length > 1 ? parts[1].trim() : "";

        Command command = commands.get(commandName);
        if (command != null) {
            command.execute(arguments, scanner, system);
        } else {
            System.out.println("Неизвестная команда: " + commandName);
            System.out.println("Введите 'help' для списка команд.");
        }
    }
}
