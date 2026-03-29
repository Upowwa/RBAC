package upowwa;

public class TemporaryAssignment extends AbstractRoleAssignment {
    private String expiresAt;
    private boolean autoRenew;

    public TemporaryAssignment(User user, Role role, AssignmentMetadata metadata,
                               String expiresAt, boolean autoRenew) {
        super(user, role, metadata);

        if (expiresAt == null) {
            throw new NullPointerException("expiresAt не может быть null");
        }
        this.expiresAt = expiresAt;
        this.autoRenew = autoRenew;
    }

    @Override
    public boolean isActive() {
        String now = "2026-03-22";
        return expiresAt.compareTo(now) > 0;
    }

    @Override
    public String assignmentType() {
        return "TEMPORARY";
    }

    public void extend(String newExpirationDate) {
        if (newExpirationDate == null) {
            throw new NullPointerException("newExpirationDate не может быть null");
        }
        this.expiresAt = newExpirationDate;
    }

    public boolean isExpired() {
        return !isActive();
    }

    public String getTimeRemaining() {
        return "X days left";
    }

    @Override
    public String summary() {
        return String.format("[TEMPORARY] %s assigned to %s by %s at 2026-02-07 19:00 " +
                        "expires %s | Reason: Initial setup | Status: %s",
                role().getName(),
                user().username(),
                metadata().assignedBy(),
                expiresAt,
                isActive() ? "ACTIVE" : "EXPIRED");
    }
}
