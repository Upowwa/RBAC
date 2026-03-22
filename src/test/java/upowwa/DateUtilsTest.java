package upowwa;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DateUtilsTest {

    @Test
    void testGetCurrentDate() {
        String date = DateUtils.getCurrentDate();
        assertNotNull(date);
        assertEquals(10, date.length()); // YYYY-MM-DD
    }

    @Test
    void testIsValidDate() {
        assertTrue(DateUtils.isValidDate("2026-03-23"));
        assertFalse(DateUtils.isValidDate("invalid"));
        assertFalse(DateUtils.isValidDate(""));
    }

    @Test
    void testAddDays() {
        assertEquals("2026-03-25", DateUtils.addDays("2026-03-23", 2));
        assertEquals("2026-03-21", DateUtils.addDays("2026-03-23", -2));
    }

    @Test
    void testIsBefore() {
        assertTrue(DateUtils.isBefore("2026-03-22", "2026-03-23"));
        assertFalse(DateUtils.isBefore("2026-03-24", "2026-03-23"));
    }
}
