package upowwa;

import java.util.*;

public class Role {
    private static int counter = 0;

    private final String id;
    private final String name;
    private final String description;
    private final Set<Permission> permissions;

    public Role(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name не может быть null или пустым");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description не может быть null или пустым");
        }

        this.id = "role_" + (++counter);
        this.name = name.trim();
        this.description = description.trim();
        this.permissions = new HashSet<>();
    }

    public void addPermission(Permission permission) {
        permissions.add(Objects.requireNonNull(permission, "Permission не может быть null"));
    }

    public void removePermission(Permission permission) {
        permissions.remove(Objects.requireNonNull(permission, "Permission не может быть null"));
    }

    public boolean hasPermission(Permission permission) {
        return permission != null && permissions.contains(permission);
    }

    public boolean hasPermission(String permissionName, String resource) {
        if (permissionName == null || resource == null) {
            return false;
        }
        return permissions.stream().anyMatch(p -> p.matches(permissionName, resource));
    }

    public Set<Permission> getPermissions() {
        return Set.copyOf(permissions);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Role role)) return false;
        return Objects.equals(id, role.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Role{id='%s', name='%s', permissions=%d}".formatted(id, name, permissions.size());
    }

    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("Role: ").append(name).append(" [ID: ").append(id).append("]\n");
        sb.append("Description: ").append(description).append("\n");
        sb.append("Permissions (").append(permissions.size()).append("):\n");
        for (Permission p : permissions) {
            sb.append("- ").append(p.format()).append("\n");
        }
        return sb.toString();
    }
}