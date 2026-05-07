package upowwa;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.*;
import java.util.stream.Collectors;

public class CommandRegistry {

    public static void registerUserCommands(CommandParser parser) {

        parser.register("user-list", (arguments, scanner, system) -> {
            UserManager userManager = system.getUserManager();
            List<User> users;

            if (arguments == null || arguments.isBlank()) {
                users = userManager.findAll();
            } else {
                String search = arguments.trim().toLowerCase();
                users = userManager.findByFilter(user ->
                        user.username().toLowerCase().contains(search)
                                || user.fullName().toLowerCase().contains(search)
                                || user.email().toLowerCase().contains(search)
                );
            }

            if (users.isEmpty()) {
                System.out.println("Пользователи не найдены.");
                return;
            }

            users = users.stream()
                    .sorted(Comparator.comparing(User::username))
                    .toList();

            System.out.printf("%-20s %-30s %-30s%n", "Username", "Full Name", "Email");
            System.out.println("--------------------------------------------------------------------------------");

            for (User user : users) {
                System.out.printf("%-20s %-30s %-30s%n",
                        user.username(),
                        user.fullName(),
                        user.email());
            }
        });

        parser.register("user-create", (arguments, scanner, system) -> {
            UserManager userManager = system.getUserManager();

            String username = ConsoleUtils.promptString(scanner, "Введите username", true);
            String fullName = ConsoleUtils.promptString(scanner, "Введите fullName", true);
            String email = ConsoleUtils.promptString(scanner, "Введите email", true);

            try {
                User user = User.create(username, fullName, email);
                userManager.add(user);
                System.out.println("Пользователь успешно создан.");
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка при создании пользователя: " + e.getMessage());
            }

            system.getAuditLog().log(
                    "USER_CREATE",
                    system.getCurrentUser(),
                    username,
                    "Создан пользователь"
            );
        });

        parser.register("user-view", (arguments, scanner, system) -> {
            UserManager userManager = system.getUserManager();
            AssignmentManager assignmentManager = system.getAssignmentManager();

            String username = ConsoleUtils.promptString(scanner, "Введите username", true);

            Optional<User> userOpt = userManager.findByUsername(username);

            if (userOpt.isEmpty()) {
                System.out.println("Пользователь не найден.");
                return;
            }

            User user = userOpt.get();

            System.out.println("Информация о пользователе:");
            System.out.println("Username: " + user.username());
            System.out.println("Full Name: " + user.fullName());
            System.out.println("Email: " + user.email());

            List<RoleAssignment> assignments = assignmentManager.findByUser(user);

            System.out.println("Назначенные роли:");
            if (assignments.isEmpty()) {
                System.out.println("- нет назначений");
            } else {
                for (RoleAssignment assignment : assignments) {
                    String status = assignment.isActive() ? "активна" : "неактивна";
                    System.out.println("- " + assignment.role().getName() + " (" + status + ")");
                }
            }

            Set<Permission> permissions = assignmentManager.getUserPermissions(user);

            System.out.println("Все права:");
            if (permissions.isEmpty()) {
                System.out.println("- нет прав");
            } else {
                permissions.stream()
                        .sorted(Comparator
                                .comparing(Permission::resource)
                                .thenComparing(Permission::name))
                        .forEach(permission ->
                                System.out.println("- " + permission.format()));
            }
        });

        parser.register("user-update", (arguments, scanner, system) -> {
            UserManager userManager = system.getUserManager();

            String username = ConsoleUtils.promptString(scanner, "Введите username", true);

            if (userManager.findByUsername(username).isEmpty()) {
                System.out.println("Пользователь не найден.");
                return;
            }

            String fullName = ConsoleUtils.promptString(scanner, "Введите fullName", true);
            String email = ConsoleUtils.promptString(scanner, "Введите email", true);

            try {
                userManager.update(username, fullName, email);
                System.out.println("Пользователь успешно обновлён.");
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка при обновлении: " + e.getMessage());
            }
        });

        parser.register("user-delete", (arguments, scanner, system) -> {
            UserManager userManager = system.getUserManager();
            AssignmentManager assignmentManager = system.getAssignmentManager();

            String username = ConsoleUtils.promptString(scanner, "Введите username", true);

            Optional<User> userOpt = userManager.findByUsername(username);

            if (userOpt.isEmpty()) {
                System.out.println("Пользователь не найден.");
                return;
            }

            User user = userOpt.get();

            boolean confirm = ConsoleUtils.promptYesNo(scanner, "Подтвердите удаление");

            if (!confirm) {
                System.out.println("Удаление отменено.");
                return;
            }

            List<RoleAssignment> assignments = assignmentManager.findByUser(user);

            for (RoleAssignment assignment : assignments) {
                assignmentManager.remove(assignment);
            }

            boolean removed = userManager.remove(user);

            if (removed) {
                System.out.println("Пользователь и все его назначения успешно удалены.");
            } else {
                System.out.println("Не удалось удалить пользователя.");
            }

            system.getAuditLog().log(
                    "USER_DELETE",
                    system.getCurrentUser(),
                    username,
                    "Пользователь удалён вместе с назначениями"
            );
        });

        parser.register("user-search", (arguments, scanner, system) -> {
            UserManager userManager = system.getUserManager();

            System.out.println("Выберите фильтр:");
            System.out.println("1. По username (содержит)");
            System.out.println("2. По email (содержит)");
            System.out.println("3. По домену email");
            System.out.println("4. По полному имени (содержит)");

            int choice = ConsoleUtils.promptInt(scanner, "Выберите фильтр", 1, 4);

            String value = ConsoleUtils.promptString(scanner, "Введите значение для поиска", true)
                    .toLowerCase();

            List<User> users = switch (choice) {
                case 1 -> userManager.findByFilter(user ->
                        user.username().toLowerCase().contains(value));

                case 2 -> userManager.findByFilter(user ->
                        user.email().toLowerCase().contains(value));

                case 3 -> userManager.findByFilter(user -> {
                    String email = user.email().toLowerCase();
                    int at = email.indexOf("@");
                    return at >= 0 && email.substring(at + 1).contains(value);
                });

                case 4 -> userManager.findByFilter(user ->
                        user.fullName().toLowerCase().contains(value));

                default -> List.of();
            };

            if (users.isEmpty()) {
                System.out.println("Пользователи не найдены.");
                return;
            }

            users = users.stream()
                    .sorted(Comparator.comparing(User::username))
                    .toList();

            System.out.printf("%-20s %-30s %-30s%n", "Username", "Full Name", "Email");
            System.out.println("--------------------------------------------------------------------------------");

            for (User user : users) {
                System.out.printf("%-20s %-30s %-30s%n",
                        user.username(),
                        user.fullName(),
                        user.email());
            }
        });

        //

        parser.registerCommand("role-list", "Вывести список всех ролей", (arguments, scanner, system) -> {
            RoleManager roleManager = system.getRoleManager();
            List<Role> roles = roleManager.findAll().stream()
                    .sorted(Comparator.comparing(Role::getName))
                    .toList();

            if (roles.isEmpty()) {
                System.out.println("Роли отсутствуют.");
                return;
            }

            System.out.printf("%-20s %-15s %-15s%n", "Name", "Permissions", "ID");
            System.out.println("------------------------------------------------------------");

            for (Role role : roles) {
                System.out.printf("%-20s %-15d %-15s%n",
                        role.getName(),
                        role.getPermissions().size(),
                        role.getId());
            }
        });

        parser.registerCommand("role-create", "Создать новую роль", (arguments, scanner, system) -> {
            RoleManager roleManager = system.getRoleManager();

            String roleName = ConsoleUtils.promptString(scanner, "Введите название роли", true);
            String description = ConsoleUtils.promptString(scanner, "Введите описание роли", true);

            try {
                Role role = new Role(roleName, description);
                roleManager.add(role);
                System.out.println("Роль успешно создана.");

                while (ConsoleUtils.promptYesNo(scanner, "Добавить право к роли?")) {
                    String permissionName = ConsoleUtils.promptString(scanner, "Введите имя права", true);
                    String resource = ConsoleUtils.promptString(scanner, "Введите ресурс", true);
                    String permissionDescription = ConsoleUtils.promptString(scanner, "Введите описание права", true);

                    try {
                        Permission permission = new Permission(permissionName, resource, permissionDescription);
                        roleManager.addPermissionToRole(roleName, permission);
                        System.out.println("Право успешно добавлено.");
                    } catch (IllegalArgumentException e) {
                        System.out.println("Ошибка при добавлении права: " + e.getMessage());
                    }
                }

                system.getAuditLog().log(
                        "ROLE_CREATE",
                        system.getCurrentUser(),
                        roleName,
                        "Создана роль: " + description
                );

            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка при создании роли: " + e.getMessage());
            }
        });

        parser.registerCommand("role-view", "Просмотр информации о роли", (arguments, scanner, system) -> {
            RoleManager roleManager = system.getRoleManager();

            String roleName = ConsoleUtils.promptString(scanner, "Введите имя роли", true);

            Optional<Role> roleOpt = roleManager.findByName(roleName);

            if (roleOpt.isEmpty()) {
                System.out.println("Роль не найдена.");
                return;
            }

            System.out.println(roleOpt.get().format());
        });

        parser.registerCommand("role-update", "Обновить название или описание роли", (arguments, scanner, system) -> {
            RoleManager roleManager = system.getRoleManager();
            AssignmentManager assignmentManager = system.getAssignmentManager();

            String oldRoleName = ConsoleUtils.promptString(scanner, "Введите текущее имя роли", true);

            Optional<Role> oldRoleOpt = roleManager.findByName(oldRoleName);

            if (oldRoleOpt.isEmpty()) {
                System.out.println("Роль не найдена.");
                return;
            }

            Role oldRole = oldRoleOpt.get();

            String newRoleName = ConsoleUtils.promptString(scanner, "Введите новое название роли", true);
            String newDescription = ConsoleUtils.promptString(scanner, "Введите новое описание роли", true);

            try {
                Role newRole = new Role(newRoleName, newDescription);

                for (Permission permission : oldRole.getPermissions()) {
                    newRole.addPermission(permission);
                }

                List<RoleAssignment> assignments = assignmentManager.findByRole(oldRole);

                if (!assignments.isEmpty()) {
                    System.out.println("Нельзя обновить роль, пока она назначена пользователям.");
                    System.out.println("Сначала снимите назначения этой роли.");
                    return;
                }

                roleManager.remove(oldRole);
                roleManager.add(newRole);

                System.out.println("Роль успешно обновлена.");
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Ошибка при обновлении роли: " + e.getMessage());
            }
        });

        parser.registerCommand("role-delete", "Удалить роль", (arguments, scanner, system) -> {
            RoleManager roleManager = system.getRoleManager();
            AssignmentManager assignmentManager = system.getAssignmentManager();

            String roleName = ConsoleUtils.promptString(scanner, "Введите имя роли", true);

            Optional<Role> roleOpt = roleManager.findByName(roleName);

            if (roleOpt.isEmpty()) {
                System.out.println("Роль не найдена.");
                return;
            }

            Role role = roleOpt.get();
            List<RoleAssignment> assignments = assignmentManager.findByRole(role).stream()
                    .filter(RoleAssignment::isActive)
                    .toList();

            if (!assignments.isEmpty()) {
                System.out.println("Внимание: роль назначена пользователям.");

                for (RoleAssignment assignment : assignments) {
                    System.out.println("- " + assignment.user().username());
                }

                boolean confirm = ConsoleUtils.promptYesNo(scanner, "Подтвердите удаление");

                if (!confirm) {
                    System.out.println("Удаление отменено.");
                    return;
                }

                System.out.println("Удаление невозможно: роль всё ещё используется в назначениях.");
                System.out.println("Сначала удалите все назначения этой роли.");
                return;
            }

            boolean confirm = ConsoleUtils.promptYesNo(scanner, "Подтвердите удаление");

            if (!confirm) {
                System.out.println("Удаление отменено.");
                return;
            }

            try {
                boolean removed = roleManager.remove(role);

                if (removed) {
                    System.out.println("Роль успешно удалена.");

                    system.getAuditLog().log(
                            "ROLE_DELETE",
                            system.getCurrentUser(),
                            roleName,
                            "Роль удалена"
                    );
                } else {
                    System.out.println("Не удалось удалить роль.");
                }
            } catch (IllegalStateException e) {
                System.out.println("Ошибка при удалении роли: " + e.getMessage());
            }
        });

        parser.registerCommand("role-add-permission", "Добавить право к роли", (arguments, scanner, system) -> {
            RoleManager roleManager = system.getRoleManager();

            String roleName = ConsoleUtils.promptString(scanner, "Введите имя роли", true);

            if (roleManager.findByName(roleName).isEmpty()) {
                System.out.println("Роль не найдена.");
                return;
            }

            String permissionName = ConsoleUtils.promptString(scanner, "Введите имя права", true);
            String resource = ConsoleUtils.promptString(scanner, "Введите ресурс", true);
            String description = ConsoleUtils.promptString(scanner, "Введите описание права", true);

            try {
                Permission permission = new Permission(permissionName, resource, description);
                roleManager.addPermissionToRole(roleName, permission);
                System.out.println("Право успешно добавлено к роли.");
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка при добавлении права: " + e.getMessage());
            }
        });

        parser.registerCommand("role-remove-permission", "Удалить право из роли", (arguments, scanner, system) -> {
            RoleManager roleManager = system.getRoleManager();

            String roleName = ConsoleUtils.promptString(scanner, "Введите имя роли", true);

            Optional<Role> roleOpt = roleManager.findByName(roleName);

            if (roleOpt.isEmpty()) {
                System.out.println("Роль не найдена.");
                return;
            }

            Role role = roleOpt.get();
            List<Permission> permissions = role.getPermissions().stream()
                    .sorted(Comparator.comparing(Permission::resource).thenComparing(Permission::name))
                    .toList();

            if (permissions.isEmpty()) {
                System.out.println("У роли нет прав.");
                return;
            }

            Permission permissionToRemove = ConsoleUtils.promptChoice(
                    scanner,
                    "Выберите право для удаления",
                    permissions
            );

            try {
                roleManager.removePermissionFromRole(roleName, permissionToRemove);
                System.out.println("Право успешно удалено.");
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка при удалении права: " + e.getMessage());
            }
        });

        parser.registerCommand("role-search", "Поиск ролей по фильтрам", (arguments, scanner, system) -> {
            RoleManager roleManager = system.getRoleManager();

            System.out.println("Выберите фильтр:");
            System.out.println("1. По имени (содержит)");
            System.out.println("2. По наличию конкретного права");
            System.out.println("3. По минимальному количеству прав");

            int choice = ConsoleUtils.promptInt(scanner, "Выберите фильтр", 1, 3);
            List<Role> roles;

            switch (choice) {
                case 1 -> {
                    String value = ConsoleUtils.promptString(scanner, "Введите часть имени роли", true)
                            .toLowerCase();

                    roles = roleManager.findByFilter(role ->
                            role.getName().toLowerCase().contains(value));
                }
                case 2 -> {
                    String permissionName = ConsoleUtils.promptString(scanner, "Введите имя права", true);
                    String resource = ConsoleUtils.promptString(scanner, "Введите ресурс", true);

                    roles = roleManager.findRolesWithPermission(permissionName, resource);
                }
                case 3 -> {
                    int minPermissions = ConsoleUtils.promptInt(scanner, "Введите минимальное количество прав", 0, Integer.MAX_VALUE);

                    roles = roleManager.findByFilter(role ->
                            role.getPermissions().size() >= minPermissions);
                }
                default -> {
                    System.out.println("Неверный вариант фильтра.");
                    return;
                }
            }

            if (roles.isEmpty()) {
                System.out.println("Роли не найдены.");
                return;
            }

            roles = roles.stream()
                    .sorted(Comparator.comparing(Role::getName))
                    .toList();

            System.out.printf("%-20s %-15s %-15s%n", "Name", "Permissions", "ID");
            System.out.println("------------------------------------------------------------");

            for (Role role : roles) {
                System.out.printf("%-20s %-15d %-15s%n",
                        role.getName(),
                        role.getPermissions().size(),
                        role.getId());
            }
        });

        //

        parser.registerCommand("assign-role", "Назначить роль пользователю", (arguments, scanner, system) -> {
            UserManager userManager = system.getUserManager();
            RoleManager roleManager = system.getRoleManager();
            AssignmentManager assignmentManager = system.getAssignmentManager();

            String username = ConsoleUtils.promptString(scanner, "Введите username", true);

            Optional<User> userOpt = userManager.findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь не найден.");
                return;
            }

            User user = userOpt.get();

            List<Role> roles = roleManager.findAll().stream()
                    .sorted(Comparator.comparing(Role::getName))
                    .toList();

            if (roles.isEmpty()) {
                System.out.println("Доступные роли отсутствуют.");
                return;
            }

            Role role = ConsoleUtils.promptChoice(scanner, "Выберите роль", roles);

            String type = ConsoleUtils.promptString(scanner, "Тип назначения (постоянное/временное)", true)
                    .toLowerCase();

            String reason = ConsoleUtils.promptString(scanner, "Причина назначения", true);

            AssignmentMetadata metadata = AssignmentMetadata.now(system.getCurrentUser(), reason);

            try {
                RoleAssignment assignment;

                if (type.equals("постоянное")) {
                    assignment = new PermanentAssignment(user, role, metadata);
                } else if (type.equals("временное")) {
                    String expirationDate = ConsoleUtils.promptString(scanner, "Введите дату истечения (YYYY-MM-DD)", true);

                    try {
                        java.time.LocalDate.parse(expirationDate);
                    } catch (Exception e) {
                        System.out.println("Некорректный формат даты. Используй YYYY-MM-DD.");
                        return;
                    }

                    assignment = new TemporaryAssignment(user, role, metadata, expirationDate, false);
                } else {
                    System.out.println("Некорректный тип назначения.");
                    return;
                }

                assignmentManager.add(assignment);
                System.out.println("Назначение успешно создано. ID: " + assignment.assignmentId());

                system.getAuditLog().log(
                        "ROLE_ASSIGN",
                        system.getCurrentUser(),
                        username,
                        "Назначена роль " + role.getName() + " (" + type + ")"
                );

            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка при создании назначения: " + e.getMessage());
            }
        });

        parser.registerCommand("revoke-role", "Отозвать роль у пользователя", (arguments, scanner, system) -> {
            UserManager userManager = system.getUserManager();
            AssignmentManager assignmentManager = system.getAssignmentManager();

            String username = ConsoleUtils.promptString(scanner, "Введите username", true);

            Optional<User> userOpt = userManager.findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь не найден.");
                return;
            }

            User user = userOpt.get();

            List<RoleAssignment> activeAssignments = assignmentManager.findByUser(user).stream()
                    .filter(RoleAssignment::isActive)
                    .toList();

            if (activeAssignments.isEmpty()) {
                System.out.println("У пользователя нет активных назначений.");
                return;
            }

            RoleAssignment assignment = ConsoleUtils.promptChoice(
                    scanner,
                    "Выберите назначение для отзыва",
                    activeAssignments
            );

            try {
                if (assignment instanceof PermanentAssignment) {
                    assignmentManager.revokeAssignment(assignment.assignmentId());
                    System.out.println("Постоянное назначение отозвано.");
                } else if (assignment instanceof TemporaryAssignment tempAssignment) {
                    String yesterday = java.time.LocalDate.now().minusDays(1).toString();
                    tempAssignment.extend(yesterday);
                    System.out.println("Временное назначение помечено как неактивное.");
                } else {
                    System.out.println("Неизвестный тип назначения.");
                    return;
                }

                system.getAuditLog().log(
                        "ROLE_REVOKE",
                        system.getCurrentUser(),
                        username,
                        "Отозвана роль " + assignment.role().getName()
                );

            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка при отзыве назначения: " + e.getMessage());
            }
        });

        parser.registerCommand("assignment-list", "Вывести список всех назначений", (arguments, scanner, system) -> {
            AssignmentManager assignmentManager = system.getAssignmentManager();

            List<RoleAssignment> assignments = assignmentManager.findAll();

            if (assignments.isEmpty()) {
                System.out.println("Назначения отсутствуют.");
                return;
            }

            System.out.printf("%-15s %-15s %-15s %-12s %-20s%n",
                    "Username", "Role", "Type", "Status", "Assigned At");
            System.out.println("-------------------------------------------------------------------------------");

            for (RoleAssignment assignment : assignments) {
                String type = assignment instanceof TemporaryAssignment ? "temporary" : "permanent";
                String status = assignment.isActive() ? "active" : "inactive";
                String assignedAt = assignment.metadata().assignedAt();

                System.out.printf("%-15s %-15s %-15s %-12s %-20s%n",
                        assignment.user().username(),
                        assignment.role().getName(),
                        type,
                        status,
                        assignedAt);
            }
        });

        parser.registerCommand("assignment-list-user", "Показать назначения конкретного пользователя", (arguments, scanner, system) -> {
            UserManager userManager = system.getUserManager();
            AssignmentManager assignmentManager = system.getAssignmentManager();

            String username = ConsoleUtils.promptString(scanner, "Введите username", true);

            Optional<User> userOpt = userManager.findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь не найден.");
                return;
            }

            User user = userOpt.get();
            List<RoleAssignment> assignments = assignmentManager.findByUser(user);

            if (assignments.isEmpty()) {
                System.out.println("У пользователя нет назначений.");
                return;
            }

            for (RoleAssignment assignment : assignments) {
                String type = assignment instanceof TemporaryAssignment ? "временное" : "постоянное";
                String status = assignment.isActive() ? "активное" : "неактивное";

                System.out.println("ID: " + assignment.assignmentId());
                System.out.println("Роль: " + assignment.role().getName());
                System.out.println("Тип: " + type);
                System.out.println("Статус: " + status);
                System.out.println("Назначено: " + assignment.metadata().assignedAt());
                System.out.println("Кем назначено: " + assignment.metadata().assignedBy());
                System.out.println("Причина: " + assignment.metadata().reason());

                if (assignment instanceof TemporaryAssignment tempAssignment) {
                    System.out.println("Истекает: " + tempAssignment.getExpiresAt());
                }

                System.out.println("----------------------------------------");
            }
        });

        parser.registerCommand("assignment-list-role", "Показать пользователей с конкретной ролью", (arguments, scanner, system) -> {
            RoleManager roleManager = system.getRoleManager();
            AssignmentManager assignmentManager = system.getAssignmentManager();

            String roleName = ConsoleUtils.promptString(scanner, "Введите имя роли", true);

            Optional<Role> roleOpt = roleManager.findByName(roleName);
            if (roleOpt.isEmpty()) {
                System.out.println("Роль не найдена.");
                return;
            }

            Role role = roleOpt.get();
            List<RoleAssignment> assignments = assignmentManager.findByRole(role);

            if (assignments.isEmpty()) {
                System.out.println("У этой роли нет назначений.");
                return;
            }

            System.out.println(FormatUtils.formatHeader("Пользователи с ролью: " + role.getName()));

            String[] headers = {"Username", "Type", "Status"};
            List<String[]> rows = assignments.stream()
                    .map(a -> new String[]{
                            a.user().username(),
                            a instanceof TemporaryAssignment ? "temporary" : "permanent",
                            a.isActive() ? "active" : "inactive"
                    })
                    .toList();

            System.out.println(FormatUtils.formatTable(headers, rows));
        });

        parser.registerCommand("assignment-active", "Показать только активные назначения", (arguments, scanner, system) -> {
            AssignmentManager assignmentManager = system.getAssignmentManager();

            List<RoleAssignment> assignments = assignmentManager.getActiveAssignments();

            if (assignments.isEmpty()) {
                System.out.println("Активных назначений нет.");
                return;
            }

            for (RoleAssignment assignment : assignments) {
                System.out.println(assignment.assignmentId() + " | "
                        + assignment.user().username() + " | "
                        + assignment.role().getName());
            }
        });

        parser.registerCommand("assignment-expired", "Показать истёкшие временные назначения", (arguments, scanner, system) -> {
            AssignmentManager assignmentManager = system.getAssignmentManager();

            List<RoleAssignment> assignments = assignmentManager.getExpiredAssignments();

            if (assignments.isEmpty()) {
                System.out.println("Истёкших назначений нет.");
                return;
            }

            for (RoleAssignment assignment : assignments) {
                System.out.println(assignment.assignmentId() + " | "
                        + assignment.user().username() + " | "
                        + assignment.role().getName());
            }
        });

        parser.registerCommand("assignment-extend", "Продлить временное назначение", (arguments, scanner, system) -> {
            AssignmentManager assignmentManager = system.getAssignmentManager();

            String assignmentId = ConsoleUtils.promptString(scanner, "Введите assignment ID", true);

            Optional<RoleAssignment> assignmentOpt = assignmentManager.findById(assignmentId);
            if (assignmentOpt.isEmpty()) {
                System.out.println("Назначение не найдено.");
                return;
            }

            RoleAssignment assignment = assignmentOpt.get();

            if (!(assignment instanceof TemporaryAssignment)) {
                System.out.println("Продлить можно только временное назначение.");
                return;
            }

            String newExpirationDate = ConsoleUtils.promptString(scanner, "Введите новую дату истечения (YYYY-MM-DD)", true);

            try {
                java.time.LocalDate.parse(newExpirationDate);
            } catch (Exception e) {
                System.out.println("Некорректный формат даты. Используй YYYY-MM-DD.");
                return;
            }

            try {
                assignmentManager.extendTemporaryAssignment(assignmentId, newExpirationDate);
                System.out.println("Временное назначение продлено.");
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка при продлении: " + e.getMessage());
            }
        });

        parser.registerCommand("assignment-search", "Поиск назначений по фильтрам", (arguments, scanner, system) -> {
            AssignmentManager assignmentManager = system.getAssignmentManager();

            System.out.println("Выберите фильтр:");
            System.out.println("1. По пользователю");
            System.out.println("2. По роли");
            System.out.println("3. По типу (постоянное/временное)");
            System.out.println("4. По статусу (активное/неактивное)");
            System.out.println("5. Назначённые после даты");
            System.out.println("6. Истекающие до даты");

            int choice = ConsoleUtils.promptInt(scanner, "Выберите фильтр", 1, 6);
            List<RoleAssignment> assignments;

            switch (choice) {
                case 1 -> {
                    String username = ConsoleUtils.promptString(scanner, "Введите username", true)
                            .toLowerCase();

                    assignments = assignmentManager.findByFilter(a ->
                            a.user().username().toLowerCase().contains(username));
                }
                case 2 -> {
                    String roleName = ConsoleUtils.promptString(scanner, "Введите имя роли", true)
                            .toLowerCase();

                    assignments = assignmentManager.findByFilter(a ->
                            a.role().getName().toLowerCase().contains(roleName));
                }
                case 3 -> {
                    String type = ConsoleUtils.promptString(scanner, "Введите тип (постоянное/временное)", true)
                            .toLowerCase();

                    assignments = assignmentManager.findByFilter(a ->
                            (type.equals("постоянное") && a instanceof PermanentAssignment)
                                    || (type.equals("временное") && a instanceof TemporaryAssignment));
                }
                case 4 -> {
                    String status = ConsoleUtils.promptString(scanner, "Введите статус (активное/неактивное)", true)
                            .toLowerCase();

                    assignments = assignmentManager.findByFilter(a ->
                            (status.equals("активное") && a.isActive())
                                    || (status.equals("неактивное") && !a.isActive()));
                }
                case 5 -> {
                    String date = ConsoleUtils.promptString(scanner, "Введите дату (YYYY-MM-DD)", true);

                    try {
                        java.time.LocalDate.parse(date);
                    } catch (Exception e) {
                        System.out.println("Некорректный формат даты. Используй YYYY-MM-DD.");
                        return;
                    }

                    assignments = assignmentManager.findByFilter(a ->
                            DateUtils.isAfter(a.metadata().assignedAt(), date));
                }
                case 6 -> {
                    String date = ConsoleUtils.promptString(scanner, "Введите дату (YYYY-MM-DD)", true);

                    try {
                        java.time.LocalDate.parse(date);
                    } catch (Exception e) {
                        System.out.println("Некорректный формат даты. Используй YYYY-MM-DD.");
                        return;
                    }

                    assignments = assignmentManager.findByFilter(a ->
                            a instanceof TemporaryAssignment temp
                                    && DateUtils.isBefore(temp.getExpiresAt(), date));
                }
                default -> {
                    System.out.println("Неверный вариант фильтра.");
                    return;
                }
            }

            if (assignments.isEmpty()) {
                System.out.println("Назначения не найдены.");
                return;
            }

            for (RoleAssignment assignment : assignments) {
                String type = assignment instanceof TemporaryAssignment ? "temporary" : "permanent";
                String status = assignment.isActive() ? "active" : "inactive";

                System.out.println(assignment.assignmentId() + " | "
                        + assignment.user().username() + " | "
                        + assignment.role().getName() + " | "
                        + type + " | "
                        + status);
            }
        });

        //

        parser.registerCommand("permissions-user", "Показать все права пользователя", (arguments, scanner, system) -> {
            UserManager userManager = system.getUserManager();
            AssignmentManager assignmentManager = system.getAssignmentManager();

            String username = ConsoleUtils.promptString(scanner, "Введите username", true);

            Optional<User> userOpt = userManager.findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь не найден.");
                return;
            }

            User user = userOpt.get();

            List<RoleAssignment> activeAssignments = assignmentManager.findByUser(user).stream()
                    .filter(RoleAssignment::isActive)
                    .toList();

            if (activeAssignments.isEmpty()) {
                System.out.println("У пользователя нет активных назначений.");
                return;
            }

            Map<String, Set<Permission>> permissionsByResource = activeAssignments.stream()
                    .flatMap(a -> a.role().getPermissions().stream())
                    .collect(Collectors.groupingBy(
                            Permission::resource,
                            TreeMap::new,
                            Collectors.toCollection(LinkedHashSet::new)
                    ));

            if (permissionsByResource.isEmpty()) {
                System.out.println("У пользователя нет прав.");
                return;
            }

            System.out.println("Права пользователя " + user.username() + ":");
            for (Map.Entry<String, Set<Permission>> entry : permissionsByResource.entrySet()) {
                System.out.println("Ресурс: " + entry.getKey());
                for (Permission permission : entry.getValue()) {
                    System.out.println("- " + permission.name()
                            + (permission.description().isBlank() ? "" : " — " + permission.description()));
                }
                System.out.println();
            }
        });

        parser.registerCommand("permissions-check", "Проверить наличие права у пользователя", (arguments, scanner, system) -> {
            UserManager userManager = system.getUserManager();
            AssignmentManager assignmentManager = system.getAssignmentManager();

            String username = ConsoleUtils.promptString(scanner, "Введите username", true);

            Optional<User> userOpt = userManager.findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь не найден.");
                return;
            }

            User user = userOpt.get();

            String permissionName = ConsoleUtils.promptString(scanner, "Введите имя права", true);
            String resource = ConsoleUtils.promptString(scanner, "Введите ресурс", true);

            boolean hasPermission = assignmentManager.userHasPermission(user, permissionName, resource);

            if (!hasPermission) {
                System.out.println("Нет, пользователь НЕ имеет это право.");
                return;
            }

            List<String> roleNames = assignmentManager.findByUser(user).stream()
                    .filter(RoleAssignment::isActive)
                    .filter(a -> a.role().hasPermission(permissionName, resource))
                    .map(a -> a.role().getName())
                    .distinct()
                    .sorted()
                    .toList();

            System.out.println("Да, пользователь имеет это право.");

            if (roleNames.isEmpty()) {
                System.out.println("Источник права определить не удалось.");
            } else {
                System.out.println("Право получено из ролей:");
                for (String roleName : roleNames) {
                    System.out.println("- " + roleName);
                }
            }
        });

        //

        parser.registerCommand("help", "Показать список всех команд", (arguments, scanner, system) -> {
            parser.printHelp();
        });

        parser.registerCommand("stats", "Показать статистику системы", (arguments, scanner, system) -> {
            System.out.println(system.generateStatistics());
        });

        parser.registerCommand("clear", "Очистить экран", (arguments, scanner, system) -> {
            System.out.print("\033[H\033[2J");
            System.out.flush();

            for (int i = 0; i < 3; i++) {
                System.out.println();
            }
        });

        parser.registerCommand("exit", "Выход из программы", (arguments, scanner, system) -> {
            String confirm = ConsoleUtils.promptString(scanner, "Вы уверены, что хотите выйти? (да/нет)", true);

            if (!confirm.equalsIgnoreCase("да")) {
                System.out.println("Выход отменён.");
                return;
            }

            String saveConfirm = ConsoleUtils.promptString(scanner, "Сохранить данные перед выходом? (да/нет)", true);

            if (saveConfirm.equalsIgnoreCase("да")) {
                System.out.println("Сохранение пока не реализовано.");
            }

            System.out.println("Завершение программы...");
            System.exit(0);
        });

        parser.registerCommand("audit-log", "Просмотр журнала аудита", (arguments, scanner, system) -> {
            system.getAuditLog().printLog();
        });

        parser.registerCommand("report-users", "Отчёт по пользователям", (args, scanner, system) -> {
            ReportGenerator generator = new ReportGenerator();
            String report = generator.generateUserReport(system.getUserManager(), system.getAssignmentManager());
            System.out.println(report);

            String save = ConsoleUtils.promptString(scanner, "Сохранить в файл? (y/n)", true);
            if (save.equalsIgnoreCase("y")) {
                String fileName = ConsoleUtils.promptString(scanner, "Имя файла", true);
                generator.exportToFile(report, fileName);
            }
        });

        parser.registerCommand("report-roles", "Отчёт по ролям", (args, scanner, system) -> {
            ReportGenerator generator = new ReportGenerator();
            String report = generator.generateRoleReport(system.getRoleManager(), system.getAssignmentManager());
            System.out.println(report);

            String save = ConsoleUtils.promptString(scanner, "Сохранить в файл? (y/n)", true);
            if (save.equalsIgnoreCase("y")) {
                String fileName = ConsoleUtils.promptString(scanner, "Имя файла", true);
                generator.exportToFile(report, fileName);
            }
        });

        parser.registerCommand("report-matrix", "Матрица прав", (args, scanner, system) -> {
            ReportGenerator generator = new ReportGenerator();
            String report = generator.generatePermissionMatrix(system.getUserManager(), system.getAssignmentManager());
            System.out.println(report);

            String save = ConsoleUtils.promptString(scanner, "Сохранить в файл? (y/n)", true);
            if (save.equalsIgnoreCase("y")) {
                String fileName = ConsoleUtils.promptString(scanner, "Имя файла", true);
                generator.exportToFile(report, fileName);
            }
        });
    }
}