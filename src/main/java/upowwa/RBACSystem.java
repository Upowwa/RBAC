package upowwa;

public class RBACSystem {
    private final UserManager userManager;
    private final RoleManager roleManager;
    private final AssignmentManager assignmentManager;
    private final AuditLog auditLog;
    private String currentUser;

    public RBACSystem() {
        this.userManager = new UserManager();
        this.roleManager = new RoleManager();
        this.assignmentManager = new AssignmentManager(userManager, roleManager);
        this.roleManager.setAssignmentManager(this.assignmentManager);
        this.currentUser = "system";
        this.auditLog = new AuditLog();
    }

    //геттеры
    public UserManager getUserManager() { return userManager; }
    public RoleManager getRoleManager() { return roleManager; }
    public AssignmentManager getAssignmentManager() { return assignmentManager; }
    public AuditLog getAuditLog() {
        return auditLog;
    }

    public void setCurrentUser(String username) {
        if (userManager.exists(username)) {
            this.currentUser = username;
        } else {
            throw new IllegalArgumentException("Пользователь '" + username + "' не существует");
        }
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void initialize() {
        System.out.println("Инициализация RBAC системы...");

        createDefaultRoles();
        createAdminUser();
        assignAdminRole();

        System.out.println("Система инициализирована");
    }

    private void createDefaultRoles() {
        Role admin = new Role("Admin", "Полный доступ");
        Role manager = new Role("Manager", "Управление пользователями");
        Role viewer = new Role("Viewer", "Только чтение");

        roleManager.add(admin);
        roleManager.add(manager);
        roleManager.add(viewer);

        roleManager.addPermissionToRole("Admin", new Permission("READ", "users", "Чтение пользователей"));
        roleManager.addPermissionToRole("Admin", new Permission("WRITE", "users", "Изменение пользователей"));
        roleManager.addPermissionToRole("Admin", new Permission("DELETE", "users", "Удаление пользователей"));

        roleManager.addPermissionToRole("Manager", new Permission("READ", "users", "Чтение пользователей"));
        roleManager.addPermissionToRole("Manager", new Permission("WRITE", "users", "Изменение пользователей"));

        roleManager.addPermissionToRole("Viewer", new Permission("READ", "users", "Чтение пользователей"));
        roleManager.addPermissionToRole("Viewer", new Permission("READ", "reports", "Чтение отчетов"));
    }

    private void createAdminUser() {
        if (!userManager.exists("admin")) {
            User adminUser = User.create("admin", "Системный администратор", "admin@rbac.com");
            userManager.add(adminUser);
            this.currentUser = "admin";
        }
    }

    private void assignAdminRole() {
        Role adminRole = roleManager.findByName("Admin").orElseThrow();
        User adminUser = userManager.findByUsername("admin").orElseThrow();

        AssignmentMetadata meta = AssignmentMetadata.now(currentUser, "Системная инициализация");
        PermanentAssignment adminAssignment = new PermanentAssignment(adminUser, adminRole, meta);
        assignmentManager.add(adminAssignment);
    }

    public String generateStatistics() {
        StringBuilder stats = new StringBuilder();
        stats.append("СТАТИСТИКА RBAC СИСТЕМЫ\n");
        stats.append(String.format("Пользователей: %d\n", userManager.count()));
        stats.append(String.format("Ролей: %d\n", roleManager.count()));
        stats.append(String.format("Назначений: %d\n", assignmentManager.count()));
        stats.append(String.format("Прав доступа: %d\n",
                roleManager.findAll().stream().mapToInt(role -> role.getPermissions().size()).sum()));
        stats.append(String.format("Текущий администратор: %s\n", currentUser));
        return stats.toString();
    }
}
