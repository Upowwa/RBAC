package upowwa;

import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ReportGeneratorTests {

    @Test
    void generateUserReport_emptyList_parallelStreamWorks() {
        ReportGenerator reportGenerator = new ReportGenerator();

        List<User> emptyUsers = Collections.emptyList();
        UserManager emptyUserManager = new UserManager() {
            public List<User> findAll() { return emptyUsers; }
        };

        String result = reportGenerator.generateUserReport(emptyUserManager, null);
        assertTrue(result.contains("Пользователи не найдены"));
        assertTrue(result.contains("ОТЧЁТ ПО ПОЛЬЗОВАТЕЛЯМ И РОЛЯМ"));
    }

    @Test
    void generatePermissionMatrix_emptyList_parallelStreamWorks() {
        ReportGenerator reportGenerator = new ReportGenerator();

        List<User> emptyUsers = Collections.emptyList();
        UserManager emptyUserManager = new UserManager() {
            public List<User> findAll() { return emptyUsers; }
        };

        String result = reportGenerator.generatePermissionMatrix(emptyUserManager, null);
        assertTrue(result.contains("Пользователи не найдены"));
        assertTrue(result.contains("МАТРИЦА ПРАВ ДОСТУПА"));
    }
}