package upowwa;

import java.util.*;
import java.util.stream.Collectors;

public class AssignmentManager implements Repository<RoleAssignment> {
    private final Map<String, RoleAssignment> assignments = new HashMap<>();
    private final UserManager userManager;
    private final RoleManager roleManager;

    public AssignmentManager(UserManager userManager, RoleManager roleManager) {
        this.userManager = Objects.requireNonNull(userManager);
        this.roleManager = Objects.requireNonNull(roleManager);
    }

    @Override
    public void add(RoleAssignment assignment) {
        validateAssignment(assignment);

        if (assignments.containsKey(assignment.assignmentId())) {
            throw new IllegalArgumentException("Назначение '" + assignment.assignmentId() + "' уже существует");
        }

        assignments.put(assignment.assignmentId(), assignment);
    }

    @Override
    public boolean remove(RoleAssignment assignment) {
        return assignments.remove(assignment.assignmentId()) != null;
    }

    @Override
    public Optional<RoleAssignment> findById(String id) {
        return Optional.ofNullable(assignments.get(id));
    }

    @Override
    public List<RoleAssignment> findAll() {
        return new ArrayList<>(assignments.values());
    }

    @Override
    public int count() {
        return assignments.size();
    }

    @Override
    public void clear() {
        assignments.clear();
    }

    public List<RoleAssignment> findByUser(User user) {
        return assignments.values().stream()
                .filter(a -> a.user() == user)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findByRole(Role role) {
        return assignments.values().stream()
                .filter(a -> a.role() == role)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findByFilter(AssignmentFilter filter) {
        return assignments.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findAll(AssignmentFilter filter, Comparator<RoleAssignment> sorter) {
        return assignments.values().stream()
                .filter(filter::test)
                .sorted(sorter)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> getActiveAssignments() {
        return findByFilter(AssignmentFilters.activeOnly());
    }

    public List<RoleAssignment> getExpiredAssignments() {
        return findByFilter(AssignmentFilters.inactiveOnly());
    }

    public boolean userHasRole(User user, Role role) {
        return assignments.values().stream()
                .anyMatch(a -> a.user() == user && a.role() == role && a.isActive());
    }

    public boolean userHasPermission(User user, String permissionName, String resource) {
        return assignments.values().stream()
                .filter(a -> a.user() == user && a.isActive())
                .flatMap(a -> a.role().getPermissions().stream())
                .anyMatch(p -> p.matches(permissionName, resource));
    }

    public Set<Permission> getUserPermissions(User user) {
        return assignments.values().stream()
                .filter(a -> a.user() == user && a.isActive())
                .flatMap(a -> a.role().getPermissions().stream())
                .collect(Collectors.toSet());
    }

    public void revokeAssignment(String assignmentId) {
        RoleAssignment assignment = assignments.get(assignmentId);
        if (assignment == null) {
            throw new IllegalArgumentException("Назначение '" + assignmentId + "' не найдено");
        }
        if (assignment instanceof PermanentAssignment perm) {
            perm.revoke();
        } else {
            throw new IllegalArgumentException("Только PermanentAssignment можно отозвать");
        }
    }

    public void extendTemporaryAssignment(String assignmentId, String newExpirationDate) {
        RoleAssignment assignment = assignments.get(assignmentId);
        if (assignment == null) {
            throw new IllegalArgumentException("Назначение '" + assignmentId + "' не найдено");
        }
        if (!(assignment instanceof TemporaryAssignment temp)) {
            throw new IllegalArgumentException("Только TemporaryAssignment можно продлить");
        }
        temp.extend(newExpirationDate);
    }

    private void validateAssignment(RoleAssignment assignment) {
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment не может быть null");
        }

        //проверка существования пользователя и роли
        if (!userManager.exists(assignment.user().username())) {
            throw new IllegalArgumentException("Пользователь '" + assignment.user().username() + "' не существует");
        }
        if (!roleManager.exists(assignment.role().getName())) {
            throw new IllegalArgumentException("Роль '" + assignment.role().getName() + "' не существует");
        }

        //проверка дублирования активных назначений той же роли
        if (assignments.values().stream()
                .anyMatch(a -> a != assignment &&
                        a.user() == assignment.user() &&
                        a.role() == assignment.role() &&
                        a.isActive())) {
            throw new IllegalArgumentException("У пользователя уже есть активное назначение роли '" +
                    assignment.role().getName() + "'");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssignmentManager that = (AssignmentManager) o;
        return assignments.equals(that.assignments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assignments);
    }
}
