package upowwa;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class FormatUtilsTest {

    @Test
    void testFormatTable() {
        String[] headers = {"ID", "Name", "Status"};
        List<String[]> rows = Arrays.asList(
                new String[]{"1", "John", "Active"},
                new String[]{"2", "Jane Doe", "Inactive"}
        );

        String table = FormatUtils.formatTable(headers, rows);

        // ТОЧНЫЕ строки из реального вывода
        assertTrue(table.contains("| ID | Name     | Status   |"), "Заголовок");
        assertTrue(table.contains("| 1  | John     | Active   |"), "Строка 1");
        assertTrue(table.contains("| 2  | Jane Doe | Inactive |"), "Строка 2");
        assertTrue(table.contains("+----+"), "Рамки слева");
        assertTrue(table.contains("+----------+"), "Рамки справа");
    }

    @Test
    void testFormatTableEmpty() {
        String table = FormatUtils.formatTable(null, Arrays.asList());
        assertEquals("Пустая таблица", table);
    }

    @Test
    void testFormatBox() {
        String box = FormatUtils.formatBox("Test");
        assertTrue(box.contains("| Test |"));
        assertTrue(box.startsWith("+"));
    }

    @Test
    void testTruncate() {
        assertEquals("abc", FormatUtils.truncate("abc", 5));
        assertEquals("ab...", FormatUtils.truncate("abcdef", 5));
    }
}
