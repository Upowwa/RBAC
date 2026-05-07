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

            System.out.print("Введите username: ");
            String username = scanner.nextLine().trim();

            System.out.print("Введите fullName: ");
            String fullName = scanner.nextLine().trim();

            System.out.print("Введите email: ");
            String email = scanner.nextLine().trim();

            try {
                User user = User.create(username, fullName, email);
                userManager.add(user);
                System.out.println("Пользователь успешно создан.");
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка при создании пользователя: " + e.getMessage());
            }
        });

        parser.register("user-view", (arguments, scanner, system) -> {
            UserManager userManager = system.getUserManager();
            AssignmentManager assignmentManager = system.getAssignmentManager();

            System.out.print("Введите username: ");
            String username = scanner.nextLine().trim();

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

            System.out.print("Введите username: ");
            String username = scanner.nextLine().trim();

            if (userManager.findByUsername(username).isEmpty()) {
                System.out.println("Пользователь не найден.");
                return;
            }

            System.out.print("Введите новый fullName: ");
            String newFullName = scanner.nextLine().trim();

            System.out.print("Введите новый email: ");
            String newEmail = scanner.nextLine().trim();

            try {
                userManager.update(username, newFullName, newEmail);
                System.out.println("Пользователь успешно обновлён.");
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка при обновлении: " + e.getMessage());
            }
        });

        parser.register("user-delete", (arguments, scanner, system) -> {
            UserManager userManager = system.getUserManager();
            AssignmentManager assignmentManager = system.getAssignmentManager();

            System.out.print("Введите username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = userManager.findByUsername(username);

            if (userOpt.isEmpty()) {
                System.out.println("Пользователь не найден.");
                return;
            }

            User user = userOpt.get();

            System.out.print("Подтвердите удаление (введите \"да\"): ");
            String confirm = scanner.nextLine().trim();

            if (!confirm.equalsIgnoreCase("да")) {
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
        });

        parser.register("user-search", (arguments, scanner, system) -> {
            UserManager userManager = system.getUserManager();

            System.out.println("Выберите фильтр:");
            System.out.println("1. По username (содержит)");
            System.out.println("2. По email (содержит)");
            System.out.println("3. По домену email");
            System.out.println("4. По полному имени (содержит)");
            System.out.print("Ваш выбор: ");

            String choice = scanner.nextLine().trim();

            System.out.print("Введите значение для поиска: ");
            String value = scanner.nextLine().trim().toLowerCase();

            if (value.isBlank()) {
                System.out.println("Фильтр не может быть пустым.");
                return;
            }

            List<User> users = switch (choice) {
                case "1" -> userManager.findByFilter(user ->
                        user.username().toLowerCase().contains(value));
                case "2" -> userManager.findByFilter(user ->
                        user.email().toLowerCase().contains(value));
                case "3" -> userManager.findByFilter(user -> {
                    String email = user.email().toLowerCase();
                    int at = email.indexOf("@");
                    return at >= 0 && email.substring(at + 1).contains(value);
                });
                case "4" -> userManager.findByFilter(user ->
                        user.fullName().toLowerCase().contains(value));
                default -> null;
            };

            if (users == null) {
                System.out.println("Неверный вариант фильтра.");
                return;
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

            System.out.print("Введите название роли: ");
            String roleName = scanner.nextLine().trim();

            System.out.print("Введите описание роли: ");
            String description = scanner.nextLine().trim();

            try {
                Role role = new Role(roleName, description);
                roleManager.add(role);
                System.out.println("Роль успешно создана.");

                while (true) {
                    System.out.print("Добавить право к роли? (да/нет): ");
                    String answer = scanner.nextLine().trim();

                    if (!answer.equalsIgnoreCase("да")) {
                        break;
                    }

                    System.out.print("Введите имя права: ");
                    String permissionName = scanner.nextLine().trim();

                    System.out.print("Введите ресурс: ");
                    String resource = scanner.nextLine().trim();

                    System.out.print("Введите описание права: ");
                    String permissionDescription = scanner.nextLine().trim();

                    try {
                        Permission permission = new Permission(permissionName, resource, permissionDescription);
                        roleManager.addPermissionToRole(roleName, permission);
                        System.out.println("Право успешно добавлено.");
                    } catch (IllegalArgumentException e) {
                        System.out.println("Ошибка при добавлении права: " + e.getMessage());
                    }
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка при создании роли: " + e.getMessage());
            }
        });

        parser.registerCommand("role-view", "Просмотр информации о роли", (arguments, scanner, system) -> {
            RoleManager roleManager = system.getRoleManager();

            System.out.print("Введите имя роли: ");
            String roleName = scanner.nextLine().trim();

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

            System.out.print("Введите текущее имя роли: ");
            String oldRoleName = scanner.nextLine().trim();

            Optional<Role> oldRoleOpt = roleManager.findByName(oldRoleName);

            if (oldRoleOpt.isEmpty()) {
                System.out.println("Роль не найдена.");
                return;
            }

            Role oldRole = oldRoleOpt.get();

            System.out.print("Введите новое название роли: ");
            String newRoleName = scanner.nextLine().trim();

            System.out.print("Введите новое описание роли: ");
            String newDescription = scanner.nextLine().trim();

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

            System.out.print("Введите имя роли: ");
            String roleName = scanner.nextLine().trim();

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

                System.out.print("Подтвердите удаление (введите \"да\"): ");
                String confirm = scanner.nextLine().trim();

                if (!confirm.equalsIgnoreCase("да")) {
                    System.out.println("Удаление отменено.");
                    return;
                }

                System.out.println("Удаление невозможно: роль всё ещё используется в назначениях.");
                System.out.println("Сначала удалите все назначения этой роли.");
                return;
            }

            System.out.print("Подтвердите удаление (введите \"да\"): ");
            String confirm = scanner.nextLine().trim();

            if (!confirm.equalsIgnoreCase("да")) {
                System.out.println("Удаление отменено.");
                return;
            }

            try {
                boolean removed = roleManager.remove(role);
                if (removed) {
                    System.out.println("Роль успешно удалена.");
                } else {
                    System.out.println("Не удалось удалить роль.");
                }
            } catch (IllegalStateException e) {
                System.out.println("Ошибка при удалении роли: " + e.getMessage());
            }
        });

        parser.registerCommand("role-add-permission", "Добавить право к роли", (arguments, scanner, system) -> {
            RoleManager roleManager = system.getRoleManager();

            System.out.print("Введите имя роли: ");
            String roleName = scanner.nextLine().trim();

            if (roleManager.findByName(roleName).isEmpty()) {
                System.out.println("Роль не найдена.");
                return;
            }

            System.out.print("Введите имя права: ");
            String permissionName = scanner.nextLine().trim();

            System.out.print("Введите ресурс: ");
            String resource = scanner.nextLine().trim();

            System.out.print("Введите описание права: ");
            String description = scanner.nextLine().trim();

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

            System.out.print("Введите имя роли: ");
            String roleName = scanner.nextLine().trim();

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

            System.out.println("Список прав:");
            for (int i = 0; i < permissions.size(); i++) {
                System.out.println((i + 1) + ". " + permissions.get(i).format());
            }

            System.out.print("Введите номер права для удаления: ");
            String input = scanner.nextLine().trim();

            try {
                int index = Integer.parseInt(input);

                if (index < 1 || index > permissions.size()) {
                    System.out.println("Некорректный номер.");
                    return;
                }

                Permission permissionToRemove = permissions.get(index - 1);
                roleManager.removePermissionFromRole(roleName, permissionToRemove);
                System.out.println("Право успешно удалено.");
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: нужно ввести число.");
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
            System.out.print("Ваш выбор: ");

            String choice = scanner.nextLine().trim();
            List<Role> roles;

            switch (choice) {
                case "1" -> {
                    System.out.print("Введите часть имени роли: ");
                    String value = scanner.nextLine().trim().toLowerCase();

                    roles = roleManager.findByFilter(role ->
                            role.getName().toLowerCase().contains(value));
                }
                case "2" -> {
                    System.out.print("Введите имя права: ");
                    String permissionName = scanner.nextLine().trim();

                    System.out.print("Введите ресурс: ");
                    String resource = scanner.nextLine().trim();

                    roles = roleManager.findRolesWithPermission(permissionName, resource);
                }
                case "3" -> {
                    System.out.print("Введите минимальное количество прав: ");
                    String input = scanner.nextLine().trim();

                    try {
                        int minPermissions = Integer.parseInt(input);
                        roles = roleManager.findByFilter(role ->
                                role.getPermissions().size() >= minPermissions);
                    } catch (NumberFormatException e) {
                        System.out.println("Ошибка: нужно ввести число.");
                        return;
                    }
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

            System.out.print("Введите username: ");
            String username = scanner.nextLine().trim();

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

            System.out.println("Доступные роли:");
            for (int i = 0; i < roles.size(); i++) {
                System.out.println((i + 1) + ". " + roles.get(i).getName());
            }

            System.out.print("Выберите номер роли: ");
            String roleInput = scanner.nextLine().trim();

            int roleIndex;
            try {
                roleIndex = Integer.parseInt(roleInput);
                if (roleIndex < 1 || roleIndex > roles.size()) {
                    System.out.println("Некорректный номер роли.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: нужно ввести число.");
                return;
            }

            Role role = roles.get(roleIndex - 1);

            System.out.print("Тип назначения (постоянное/временное): ");
            String type = scanner.nextLine().trim().toLowerCase();

            System.out.print("Причина назначения: ");
            String reason = scanner.nextLine().trim();

            AssignmentMetadata metadata = AssignmentMetadata.now(system.getCurrentUser(), reason);

            try {
                RoleAssignment assignment;

                if (type.equals("постоянное")) {
                    assignment = new PermanentAssignment(user, role, metadata);
                } else if (type.equals("временное")) {
                    System.out.print("Введите дату истечения: ");
                    String expirationDate = scanner.nextLine().trim();

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
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка при создании назначения: " + e.getMessage());
            }
        });

        parser.registerCommand("revoke-role", "Отозвать роль у пользователя", (arguments, scanner, system) -> {
            UserManager userManager = system.getUserManager();
            AssignmentManager assignmentManager = system.getAssignmentManager();

            System.out.print("Введите username: ");
            String username = scanner.nextLine().trim();

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

            System.out.println("Активные назначения:");
            for (int i = 0; i < activeAssignments.size(); i++) {
                RoleAssignment assignment = activeAssignments.get(i);
                String type = assignment instanceof TemporaryAssignment ? "временное" : "постоянное";
                System.out.println((i + 1) + ". " + assignment.role().getName()
                        + " | " + type
                        + " | ID: " + assignment.assignmentId());
            }

            System.out.print("Выберите номер назначения для отзыва: ");
            String input = scanner.nextLine().trim();

            try {
                int index = Integer.parseInt(input);
                if (index < 1 || index > activeAssignments.size()) {
                    System.out.println("Некорректный номер.");
                    return;
                }

                RoleAssignment assignment = activeAssignments.get(index - 1);

                if (assignment instanceof PermanentAssignment) {
                    assignmentManager.revokeAssignment(assignment.assignmentId());
                    System.out.println("Постоянное назначение отозвано.");
                } else if (assignment instanceof TemporaryAssignment tempAssignment) {
                    String yesterday = java.time.LocalDate.now().minusDays(1).toString();
                    tempAssignment.extend(yesterday);
                    System.out.println("Временное назначение помечено как неактивное.");
                } else {
                    System.out.println("Неизвестный тип назначения.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: нужно ввести число.");
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

            System.out.print("Введите username: ");
            String username = scanner.nextLine().trim();

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

            System.out.print("Введите имя роли: ");
            String roleName = scanner.nextLine().trim();

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

            System.out.printf("%-15s %-15s %-12s%n", "Username", "Type", "Status");
            System.out.println("------------------------------------------------");

            for (RoleAssignment assignment : assignments) {
                String type = assignment instanceof TemporaryAssignment ? "temporary" : "permanent";
                String status = assignment.isActive() ? "active" : "inactive";

                System.out.printf("%-15s %-15s %-12s%n",
                        assignment.user().username(),
                        type,
                        status);
            }
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

            System.out.print("Введите assignment ID: ");
            String assignmentId = scanner.nextLine().trim();

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

            System.out.print("Введите новую дату истечения: ");
            String newExpirationDate = scanner.nextLine().trim();

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
            System.out.print("Ваш выбор: ");

            String choice = scanner.nextLine().trim();
            List<RoleAssignment> assignments;

            switch (choice) {
                case "1" -> {
                    System.out.print("Введите username: ");
                    String username = scanner.nextLine().trim().toLowerCase();

                    assignments = assignmentManager.findByFilter(a ->
                            a.user().username().toLowerCase().contains(username));
                }
                case "2" -> {
                    System.out.print("Введите имя роли: ");
                    String roleName = scanner.nextLine().trim().toLowerCase();

                    assignments = assignmentManager.findByFilter(a ->
                            a.role().getName().toLowerCase().contains(roleName));
                }
                case "3" -> {
                    System.out.print("Введите тип (постоянное/временное): ");
                    String type = scanner.nextLine().trim().toLowerCase();

                    assignments = assignmentManager.findByFilter(a ->
                            (type.equals("постоянное") && a instanceof PermanentAssignment)
                                    || (type.equals("временное") && a instanceof TemporaryAssignment));
                }
                case "4" -> {
                    System.out.print("Введите статус (активное/неактивное): ");
                    String status = scanner.nextLine().trim().toLowerCase();

                    assignments = assignmentManager.findByFilter(a ->
                            (status.equals("активное") && a.isActive())
                                    || (status.equals("неактивное") && !a.isActive()));
                }
                case "5" -> {
                    System.out.print("Введите дату (YYYY-MM-DD): ");
                    String date = scanner.nextLine().trim();

                    assignments = assignmentManager.findByFilter(a ->
                            a.metadata().assignedAt().compareTo(date) > 0);
                }
                case "6" -> {
                    System.out.print("Введите дату (YYYY-MM-DD): ");
                    String date = scanner.nextLine().trim();

                    assignments = assignmentManager.findByFilter(a ->
                            a instanceof TemporaryAssignment temp && temp.getExpiresAt().compareTo(date) < 0);
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

            System.out.print("Введите username: ");
            String username = scanner.nextLine().trim();

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
                    System.out.println("- " + permission.name() +
                            (permission.description().isBlank() ? "" : " — " + permission.description()));
                }
                System.out.println();
            }
        });

        parser.registerCommand("permissions-check", "Проверить наличие права у пользователя", (arguments, scanner, system) -> {
            UserManager userManager = system.getUserManager();
            AssignmentManager assignmentManager = system.getAssignmentManager();

            System.out.print("Введите username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = userManager.findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь не найден.");
                return;
            }

            User user = userOpt.get();

            System.out.print("Введите имя права: ");
            String permissionName = scanner.nextLine().trim();

            System.out.print("Введите ресурс: ");
            String resource = scanner.nextLine().trim();

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
            System.out.print("Вы уверены, что хотите выйти? (да/нет): ");
            String confirm = scanner.nextLine().trim();

            if (!confirm.equalsIgnoreCase("да")) {
                System.out.println("Выход отменён.");
                return;
            }

            System.out.print("Сохранить данные перед выходом? (да/нет): ");
            String saveConfirm = scanner.nextLine().trim();

            if (saveConfirm.equalsIgnoreCase("да")) {
                System.out.println("Сохранение пока не реализовано.");
            }

            System.out.println("Завершение программы...");
            System.exit(0);
        });
    }
}