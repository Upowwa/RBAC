package upowwa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

class RoleManagerTest {

    private RoleManager roleManager;

    @BeforeEach
    void setUp() {
        roleManager = new RoleManager();
    }

    @Test
    void addRole_success() {
        Role role = new Role("Admin", "Администратор");
        roleManager.add(role);
        assertEquals(1, roleManager.count());
        assertTrue(roleManager.exists("Admin"));
    }

    @Test
    void addPermissionToRole_success() {
        Role role = new Role("Admin", "Администратор");
        roleManager.add(role);

        Permission perm = new Permission("READ", "users", "Чтение");
        roleManager.addPermissionToRole("Admin", perm);

        assertTrue(role.hasPermission(perm));
    }

    @Test
    void findRolesWithPermission_returnsMatchingRoles() {
        Role admin = new Role("Admin", "Полный доступ");
        Role user = new Role("User", "Обычный");
        admin.addPermission(new Permission("READ", "users", "Чтение"));

        roleManager.add(admin);
        roleManager.add(user);

        List<Role> roles = roleManager.findRolesWithPermission("READ", "users");
        assertEquals(1, roles.size());
        assertEquals("Admin", roles.get(0).getName());
    }
}
