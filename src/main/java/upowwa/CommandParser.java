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
        commands.put(name.toLowerCase(), command);
        commandDescriptions.put(name.toLowerCase(), description);
    }

    public void executeCommand(String commandName, Scanner scanner, RBACSystem system) {
        String cmdName = commandName.toLowerCase();
        Command cmd = commands.get(cmdName);

        if (cmd != null) {
            cmd.execute(scanner, system);
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
        String commandName = parts[0];

        if (commands.containsKey(commandName.toLowerCase())) {
            executeCommand(commandName, scanner, system);
        } else {
            System.out.println("Неизвестная команда: " + commandName);
            System.out.println("Введите 'help' для списка команд.");
        }
    }
}
