package upowwa;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TemporaryAssignment extends AbstractRoleAssignment {
    private String expiresAt;
    private final boolean autoRenew;

    public TemporaryAssignment(User user, Role role, AssignmentMetadata metadata,
                               String expiresAt, boolean autoRenew) {
        super(user, role, metadata);

        if (expiresAt == null || expiresAt.trim().isEmpty()) {
            throw new IllegalArgumentException("expiresAt не может быть null или пустым");
        }

        this.expiresAt = expiresAt.trim();
        this.autoRenew = autoRenew;
    }

    @Override
    public boolean isActive() {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        return expiresAt.compareTo(today) >= 0;
    }

    public boolean isActive(String currentDate) {
        if (currentDate == null || currentDate.trim().isEmpty()) {
            throw new IllegalArgumentException("currentDate не может быть null или пустым");
        }
        return expiresAt.compareTo(currentDate.trim()) >= 0;
    }

    @Override
    public String assignmentType() {
        return "TEMPORARY";
    }

    public void extend(String newExpirationDate) {
        if (newExpirationDate == null || newExpirationDate.trim().isEmpty()) {
            throw new IllegalArgumentException("newExpirationDate не может быть null или пустым");
        }
        this.expiresAt = newExpirationDate.trim();
    }

    public boolean isExpired() {
        return !isActive();
    }

    public boolean isExpired(String currentDate) {
        return !isActive(currentDate);
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public boolean isAutoRenew() {
        return autoRenew;
    }

    public String getTimeRemaining() {
        return "До " + expiresAt;
    }

    @Override
    public String summary() {
        String reason = metadata().reason().isEmpty() ? "не указана" : metadata().reason();
        String status = isActive() ? "ACTIVE" : "EXPIRED";

        return String.format(
                "[%s] %s assigned to %s by %s at %s Expires: %s Reason: %s Status: %s",
                assignmentType(),
                role().getName(),
                user().username(),
                metadata().assignedBy(),
                metadata().assignedAt(),
                expiresAt,
                reason,
                status
        );
    }
}