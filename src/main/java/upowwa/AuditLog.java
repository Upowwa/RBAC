package upowwa;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class AuditLog {
    private final List<AuditEntry> entries = new ArrayList<>();
    private final BlockingQueue<AuditEntry> queue = new LinkedBlockingQueue<>();
    private final Thread workerThread;
    private volatile boolean running = true;

    public AuditLog() {
        this.workerThread = new Thread(() -> {
            while (running || !queue.isEmpty()) {
                try {
                    AuditEntry entry = queue.take();
                    synchronized (entries) {
                        entries.add(entry);
                    }
                } catch (InterruptedException e) {
                    if (!running) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });

        this.workerThread.setName("audit-log-worker");
        this.workerThread.setDaemon(true);
        this.workerThread.start();
    }

    public void log(String action, String performer, String target, String details) {
        if (action == null || action.trim().isEmpty()) {
            throw new IllegalArgumentException("Действие не может быть пустым");
        }
        if (performer == null || performer.trim().isEmpty()) {
            throw new IllegalArgumentException("Исполнитель не может быть пустым");
        }
        queue.offer(AuditEntry.now(action, performer, target, details));
    }

    public List<AuditEntry> getAll() {
        synchronized (entries) {
            return new ArrayList<>(entries);
        }
    }

    public List<AuditEntry> getByPerformer(String performer) {
        if (performer == null || performer.trim().isEmpty()) {
            return new ArrayList<>();
        }
        synchronized (entries) {
            return entries.stream()
                    .filter(e -> e.performer().equalsIgnoreCase(performer.trim()))
                    .collect(Collectors.toList());
        }
    }

    public List<AuditEntry> getByAction(String action) {
        if (action == null || action.trim().isEmpty()) {
            return new ArrayList<>();
        }
        synchronized (entries) {
            return entries.stream()
                    .filter(e -> e.action().equalsIgnoreCase(action.trim()))
                    .collect(Collectors.toList());
        }
    }

    public int count() {
        synchronized (entries) {
            return entries.size();
        }
    }

    public void clear() {
        synchronized (entries) {
            entries.clear();
        }
    }

    public void printLog() {
        synchronized (entries) {
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
    }

    public void saveToFile(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя файла не может быть пустым");
        }
        synchronized (entries) {
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
    }

    public void loadFromFile(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя файла не может быть пустым");
        }
        try {
            List<String> lines = Files.readAllLines(Paths.get(filename));
            List<AuditEntry> loadedEntries = new ArrayList<>();

            for (String line : lines) {
                if (line.startsWith("[") && line.contains("]")) {
                    int closeBracket = line.indexOf("]");
                    String timestamp = line.substring(1, closeBracket);
                    String rest = line.substring(closeBracket + 2).trim();
                    String[] parts = rest.split(" \\| ", 4);
                    if (parts.length == 4) {
                        loadedEntries.add(new AuditEntry(timestamp, parts[0], parts[1], parts[2], parts[3]));
                    }
                }
            }

            synchronized (entries) {
                entries.addAll(loadedEntries);
            }

            System.out.println("Лог загружен из файла: " + filename + " (" + loadedEntries.size() + " записей)");
        } catch (IOException e) {
            System.out.println("Файл лога не найден или не читается: " + filename);
        }
    }

    public void shutdown() {
        running = false;
        workerThread.interrupt();
        try {
            workerThread.join(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}