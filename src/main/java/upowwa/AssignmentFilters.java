package upowwa;

public class AssignmentFilters {

    public static AssignmentFilter byUser(User user) {
        return assignment -> assignment.user() == user;
    }

    public static AssignmentFilter byUsername(String username) {
        return assignment -> assignment.user().username().equals(username);
    }

    public static AssignmentFilter byRole(Role role) {
        return assignment -> assignment.role() == role;
    }

    public static AssignmentFilter byRoleName(String roleName) {
        return assignment -> assignment.role().getName().equals(roleName);
    }

    public static AssignmentFilter activeOnly() {
        return RoleAssignment::isActive;
    }

    public static AssignmentFilter inactiveOnly() {
        return assignment -> !assignment.isActive();
    }

    public static AssignmentFilter byType(String type) {
        return assignment -> assignment.assignmentType().equals(type);
    }

    public static AssignmentFilter assignedBy(String username) {
        return assignment -> assignment.metadata().assignedBy().equals(username);
    }

    public static AssignmentFilter assignedAfter(String date) {
        return assignment -> assignment.metadata().assignedAt().compareTo(date) > 0;
    }

    public static AssignmentFilter expiringBefore(String date) {
        return assignment -> {
            try {
                if ("TEMPORARY".equals(assignment.assignmentType())) {
                    return assignment.metadata().assignedAt().compareTo(date) < 0;
                }
            } catch (Exception e) {
                return false;
            }
            return false;
        };
    }
}
