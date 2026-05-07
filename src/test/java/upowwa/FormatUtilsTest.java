package upowwa;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class FormatUtilsTest {

    @Test
    void truncate_shouldShortenLongText() {
        assertEquals("Hello...", FormatUtils.truncate("Hello world", 8));
    }

    @Test
    void padRight_shouldAppendSpaces() {
        assertEquals("abc  ", FormatUtils.padRight("abc", 5));
    }

    @Test
    void padLeft_shouldAppendSpacesOnLeft() {
        assertEquals("  abc", FormatUtils.padLeft("abc", 5));
    }

    @Test
    void formatBox_shouldContainBordersAndText() {
        String result = FormatUtils.formatBox("Hello");
        assertTrue(result.contains("+"));
        assertTrue(result.contains("| Hello |"));
    }

    @Test
    void formatHeader_shouldContainText() {
        String result = FormatUtils.formatHeader("Users");
        assertTrue(result.contains("Users"));
        assertTrue(result.contains("="));
    }

    @Test
    void formatTable_shouldContainHeadersAndRows() {
        String[] headers = {"Username", "Email"};
        List<String[]> rows = List.of(
                new String[]{"admin", "admin@mail.com"},
                new String[]{"john", "john@mail.com"}
        );

        String result = FormatUtils.formatTable(headers, rows);

        assertTrue(result.contains("Username"));
        assertTrue(result.contains("admin"));
        assertTrue(result.contains("+"));
        assertTrue(result.contains("|"));
    }
}
