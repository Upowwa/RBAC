package upowwa;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AuditLog {
    private final List<AuditEntry> entries = new ArrayList<>();

    public void log(String action, String performer, String target, String details) {
        if (action == null || action.trim().isEmpty()) {
            throw new IllegalArgumentException("Действие не может быть пустым");
        }
        if (performer == null || performer.trim().isEmpty()) {
            throw new IllegalArgumentException("Исполнитель не может быть пустым");
        }
        entries.add(AuditEntry.now(action, performer, target, details));
    }

    public List<AuditEntry> getAll() {
        return new ArrayList<>(entries);
    }

    public List<AuditEntry> getByPerformer(String performer) {
        if (performer == null || performer.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return entries.stream()
                .filter(e -> e.performer().equalsIgnoreCase(performer.trim()))
                .collect(Collectors.toList());
    }

    public List<AuditEntry> getByAction(String action) {
        if (action == null || action.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return entries.stream()
                .filter(e -> e.action().equalsIgnoreCase(action.trim()))
                .collect(Collectors.toList());
    }

    public int count() {
        return entries.size();
    }

    public void clear() {
        entries.clear();
    }

    public void printLog() {
        if (entries.isEmpty()) {
            System.out.println("Лог аудита пуст");
            return;
        }
        System.out.println("\n=== Audit Log (" + entries.size() + " entries) ===");
        System.out.printf("%-20s %-15s %-15s %-20s %s%n",
                "Timestamp", "Action", "Performer", "Target", "Details");
        System.out.println("=".repeat(90));
        for (AuditEntry entry : entries) {
            System.out.printf("%-20s %-15s %-15s %-20s %s%n",
                    entry.timestamp(),
                    entry.action(),
                    entry.performer(),
                    entry.target(),
                    entry.details());
        }
    }

    public void saveToFile(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя файла не может быть пустым");
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (AuditEntry entry : entries) {
                writer.write(entry.format());
                writer.newLine();
            }
            System.out.println("Лог сохранён в файл: " + filename);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка сохранения лога: " + e.getMessage(), e);
        }
    }

    public void loadFromFile(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя файла не может быть пустым");
        }
        try {
            List<String> lines = Files.readAllLines(Paths.get(filename));
            for (String line : lines) {
                // Простой парсинг: [timestamp] action | performer | target | details
                if (line.startsWith("[") && line.contains("]")) {
                    int closeBracket = line.indexOf("]");
                    String timestamp = line.substring(1, closeBracket);
                    String rest = line.substring(closeBracket + 2).trim();
                    String[] parts = rest.split(" \\| ", 5);
                    if (parts.length == 5) {
                        entries.add(new AuditEntry(timestamp, parts[0], parts[1], parts[2], parts[3]));
                    }
                }
            }
            System.out.println("Лог загружен из файла: " + filename + " (" + lines.size() + " записей)");
        } catch (IOException e) {
            System.out.println("Файл лога не найден или не читается: " + filename);
        }
    }
}