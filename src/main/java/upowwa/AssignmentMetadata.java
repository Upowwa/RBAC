package upowwa;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public record AssignmentMetadata(String assignedBy, String assignedAt, String reason) {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static AssignmentMetadata now(String assignedBy, String reason) {
        String currentTime = LocalDateTime.now().format(FORMATTER);
        return new AssignmentMetadata(Objects.requireNonNull(assignedBy, "assignedBy не может быть null"),
                currentTime, reason != null ? reason : "");
    }

    public String format() {
        return String.format("Назначил: %s | Время: %s | Причина: %s", assignedBy, assignedAt,
                reason.isEmpty() ? "не указана" : reason);
    }
}
