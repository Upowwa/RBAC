package upowwa;

import java.util.*;
import java.util.stream.Collectors;

public class RoleManager implements Repository<Role> {
    private final Map<String, Role> rolesById = new HashMap<>();  // ключ — id роли
    private final Map<String, Role> rolesByName = new HashMap<>(); // ключ — имя роли

    @Override
    public void add(Role role) {
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
    public boolean remove(Role role) {
        // Проверка: роль используется в назначениях?
        // Упрощенно - просто удаляем (проверка в AssignmentManager)

        boolean removedById = rolesById.remove(role.getId()) != null;
        boolean removedByName = rolesByName.remove(role.getName()) != null;

        return removedById || removedByName;
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
    public void clear() {
        rolesById.clear();
        rolesByName.clear();
    }

    // Дополнительные методы
    public Optional<Role> findByName(String name) {
        return Optional.ofNullable(rolesByName.get(name));
    }

    public List<Role> findByFilter(RoleFilter filter) {
        return rolesById.values().stream()
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
        if (o == null || getClass() != o.getClass()) return false;
        RoleManager that = (RoleManager) o;
        return rolesById.equals(that.rolesById);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rolesById);
    }
}
