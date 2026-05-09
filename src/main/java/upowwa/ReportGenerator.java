package upowwa;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ReportGenerator {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String generateUserReport(UserManager userManager, AssignmentManager assignmentManager) {
        List<User> users = userManager.findAll();

        StringBuilder sb = new StringBuilder();
        sb.append("ОТЧЁТ ПО ПОЛЬЗОВАТЕЛЯМ И РОЛЯМ\n");
        sb.append("Сформирован: ").append(LocalDateTime.now().format(TIMESTAMP_FORMATTER)).append("           \n");

        if (users.isEmpty()) {
            sb.append("Пользователи не найдены\n");
            return sb.toString();
        }

        sb.append(String.format("%-20s %-25s %-30s %-15s%n", "Username", "Full Name", "Email", "Roles"));
        sb.append("═".repeat(90)).append("\n");

        String usersBlock = users.parallelStream()
                .map(user -> {
                    List<RoleAssignment> assignments = assignmentManager.findByUser(user);
                    List<String> roleNames = assignments.stream()
                            .filter(RoleAssignment::isActive)
                            .map(a -> a.role().getName())
                            .collect(Collectors.toList());

                    String rolesStr = roleNames.isEmpty() ? "Нет ролей" : String.join(", ", roleNames);

                    return String.format("%-20s %-25s %-30s %-15s%n",
                            user.username(),
                            truncate(user.fullName(), 24),
                            truncate(user.email(), 29),
                            truncate(rolesStr, 14));
                })
                .collect(Collectors.joining());

        sb.append(usersBlock);

        sb.append("\n").append("Всего пользователей: ").append(users.size()).append("\n");

        long usersWithRoles = users.parallelStream()
                .filter(u -> !assignmentManager.findByUser(u).stream()
                        .filter(RoleAssignment::isActive)
                        .collect(Collectors.toList()).isEmpty())
                .count();

        sb.append("Пользователей с ролями: ").append(usersWithRoles).append("\n");

        return sb.toString();
    }

    public String generateRoleReport(RoleManager roleManager, AssignmentManager assignmentManager) {
        List<Role> roles = roleManager.findAll();
        StringBuilder report = new StringBuilder();
        report.append("=== ОТЧЁТ ПО РОЛЯМ ===\n\n");

        if (roles.isEmpty()) {
            report.append("Роли отсутствуют\n");
            return report.toString();
        }

        roles.parallelStream()
                .forEach(role -> {
                    long count = assignmentManager.findByRole(role).parallelStream()
                            .filter(RoleAssignment::isActive)
                            .map(ra -> ra.user().username())
                            .distinct()
                            .count();

                    report.append(String.format("Роль: %-15s | Пользователей: %d | Права: %s\n",
                            role.toString(),
                            (int) count,
                            role.getPermissions().stream()
                                    .map(Permission::toString)
                                    .collect(Collectors.joining(", "))));
                });

        return report.toString();
    }

    public String generatePermissionMatrix(UserManager userManager, AssignmentManager assignmentManager) {
        List<User> users = userManager.findAll();

        StringBuilder sb = new StringBuilder();
        sb.append("МАТРИЦА ПРАВ ДОСТУПА\n");
        sb.append("Сформирован: ").append(LocalDateTime.now().format(TIMESTAMP_FORMATTER)).append("           \n");

        if (users.isEmpty()) {
            sb.append("Пользователи не найдены\n");
            return sb.toString();
        }

        Set<String> allResources = users.parallelStream()
                .flatMap(user -> assignmentManager.getUserPermissions(user).stream())
                .map(Permission::resource)
                .collect(Collectors.toCollection(TreeSet::new));

        if (allResources.isEmpty()) {
            sb.append("Права доступа не найдены\n");
            return sb.toString();
        }

        sb.append(String.format("%-20s", "Username"));
        for (String resource : allResources) {
            sb.append(String.format(" %-10s", truncate(resource.toUpperCase(), 10)));
        }
        sb.append("\n");
        sb.append("═".repeat(20 + allResources.size() * 11)).append("\n");

        String matrixBlock = users.parallelStream()
                .map(user -> {
                    Set<String> userResources = assignmentManager.getUserPermissions(user).stream()
                            .map(Permission::resource)
                            .collect(Collectors.toSet());

                    StringBuilder row = new StringBuilder();
                    row.append(String.format("%-20s", user.username()));
                    for (String resource : allResources) {
                        String mark = userResources.contains(resource) ? "✓" : "✗";
                        row.append(String.format(" %-10s", mark));
                    }
                    row.append("\n");
                    return row.toString();
                })
                .collect(Collectors.joining());

        sb.append(matrixBlock);
        sb.append("\n✓ — есть доступ, ✗ — нет доступа\n");
        sb.append("Всего ресурсов: ").append(allResources.size()).append("\n");

        return sb.toString();
    }

    public void exportToFile(String report, String filename) {
        if (report == null || report.trim().isEmpty()) {
            throw new IllegalArgumentException("Отчёт не может быть пустым");
        }
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя файла не может быть пустым");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(report);
            System.out.println("Отчёт сохранён в файл: " + filename);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка сохранения отчёта: " + e.getMessage(), e);
        }
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }
}