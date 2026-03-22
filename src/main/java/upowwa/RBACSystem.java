package upowwa;

public class RBACSystem {
    public final UserManager userManager;
    public final RoleManager roleManager;
    public final AssignmentManager assignmentManager;

    public RBACSystem() {
        this.userManager = new UserManager();
        this.roleManager = new RoleManager();
        this.assignmentManager = new AssignmentManager(userManager, roleManager);
    }
}