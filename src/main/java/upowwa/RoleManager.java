package upowwa;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RoleManager implements Repository<Role> {
    private final Map<String, Role> rolesById = new ConcurrentHashMap<>();
    private final Map<String, Role> rolesByName = new ConcurrentHashMap<>();
    private volatile AssignmentManager assignmentManager;

    public RoleManager() {
    }
    public void setAssignmentManager(AssignmentManager assignmentManager) {
        this.assignmentManager = assignmentManager;
    }

    @Override
    public synchronized void add(Role role) {
        validateRole(role);

        if (rolesById.containsKey(role.getId())) {
            throw new IllegalArgumentException("Роль с id '" + role.getId() + "' уже существует");
        }
        if (rolesByName.containsKey(role.getName())) {
            throw new IllegalArgumentException("Роль с именем '" + role.getName() + "' уже существует");
        }

        rolesById.put(role.getId(), role);
        rolesByName.put(role.getName(), role);
    }

    @Override
    public synchronized boolean remove(Role role) {
        validateRole(role);

        Role existing = rolesById.get(role.getId());
        if (existing == null) {
            return false;
        }

        if (assignmentManager != null && assignmentManager.hasRoleInUse(role.getName())) {
            throw new IllegalStateException(
                    "Роль '" + role.getName() + "' используется в назначениях и не может быть удалена"
            );
        }

        rolesById.remove(role.getId());
        rolesByName.remove(role.getName());
        return true;
    }

    @Override
    public Optional<Role> findById(String id) {
        return Optional.ofNullable(rolesById.get(id));
    }

    @Override
    public List<Role> findAll() {
        return new ArrayList<>(rolesById.values());
    }

    @Override
    public int count() {
        return rolesById.size();
    }

    @Override
    public synchronized void clear() {
        rolesById.clear();
        rolesByName.clear();
    }

    public Optional<Role> findByName(String name) {
        return Optional.ofNullable(rolesByName.get(name));
    }

    public List<Role> findByFilter(RoleFilter filter) {
        Objects.requireNonNull(filter, "Filter не может быть null");
        return rolesById.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<Role> findByFilterParallel(RoleFilter filter) {
        Objects.requireNonNull(filter, "Filter не может быть null");
        return findAll().parallelStream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<Role> findAll(RoleFilter filter, Comparator<Role> sorter) {
        return rolesById.values().stream()
                .filter(filter::test)
                .sorted(sorter)
                .collect(Collectors.toList());
    }

    public boolean exists(String name) {
        return rolesByName.containsKey(name);
    }

    public void addPermissionToRole(String roleName, Permission permission) {
        Role role = rolesByName.get(roleName);
        if (role == null) {
            throw new IllegalArgumentException("Роль '" + roleName + "' не найдена");
        }
        role.addPermission(permission);
    }

    public void removePermissionFromRole(String roleName, Permission permission) {
        Role role = rolesByName.get(roleName);
        if (role == null) {
            throw new IllegalArgumentException("Роль '" + roleName + "' не найдена");
        }
        role.removePermission(permission);
    }

    public List<Role> findRolesWithPermission(String permissionName, String resource) {
        return rolesById.values().stream()
                .filter(role -> role.hasPermission(permissionName, resource))
                .collect(Collectors.toList());
    }

    private void validateRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role не может быть null");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoleManager that)) return false;
        return rolesById.equals(that.rolesById) && rolesByName.equals(that.rolesByName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rolesById, rolesByName);
    }
}
