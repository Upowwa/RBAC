package upowwa;

import java.util.Objects;

public abstract class AbstractRoleAssignment implements RoleAssignment {
    private static int assignmentCounter = 0;
    private final String assignmentId;
    private final User user;
    private final Role role;
    private final AssignmentMetadata metadata;

    public AbstractRoleAssignment(User user, Role role, AssignmentMetadata metadata) {
        this.assignmentId = "assign_" + (++assignmentCounter);
        this.user = Objects.requireNonNull(user, "User не может быть null");
        this.role = Objects.requireNonNull(role, "Role не может быть null");
        this.metadata = Objects.requireNonNull(metadata, "Metadata не может быть null");
    }

    @Override
    public String assignmentId() {
        return assignmentId;
    }

    @Override
    public User user() {
        return user;
    }

    @Override
    public Role role() {
        return role;
    }

    @Override
    public AssignmentMetadata metadata() {
        return metadata;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof RoleAssignment)) return false;
        return Objects.equals(assignmentId(), ((RoleAssignment) obj).assignmentId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(assignmentId());
    }

    public String summary() {
        return String.format("[PERMANENT] %s assigned to %s by %s at 2026-02-07 19:00 Reason: Initial setup Status: ACTIVE",
                role.getName(), user.username(), metadata.assignedBy());
    }

    @Override
    public abstract boolean isActive();

    @Override
    public abstract String assignmentType();
}
