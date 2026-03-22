package upowwa;
import java.util.*;

public class Role {
    private static int counter = 0;
    private final String id;
    private final String name;
    private final String description;
    private final Set<Permission> permissions;

    public Role(String name, String description) {
        this.id = "role_" + (++counter);
        this.name = Objects.requireNonNull(name, "Name не может быть null");
        this.description = Objects.requireNonNull(description, "Description не может быть null");
        this.permissions = new HashSet<>();
    }

    public void addPermission(Permission permission) {
        permissions.add(Objects.requireNonNull(permission, "Permission не может быть null"));
    }

    public void removePermission(Permission permission) {
        permissions.remove(Objects.requireNonNull(permission, "Permission не может быть null"));
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }

    public boolean hasPermission(String permissionName, String resource) {
        return permissions.stream().anyMatch(p -> p.matches(permissionName, resource));
    }

    public Set<Permission> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Role role = (Role) obj;
        return Objects.equals(id, role.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Role{id='%s', name='%s', permissions=%d}", id, name, permissions.size());
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

    public String getName() {
        return name;
    }
    public String getId() { return id; }
}
