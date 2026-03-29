package upowwa;

@FunctionalInterface
public interface AssignmentFilter {
    boolean test(RoleAssignment assignment);

    default AssignmentFilter and(AssignmentFilter other) {
        return assignment -> test(assignment) && other.test(assignment);
    }

    default AssignmentFilter or(AssignmentFilter other) {
        return assignment -> test(assignment) || other.test(assignment);
    }
}
