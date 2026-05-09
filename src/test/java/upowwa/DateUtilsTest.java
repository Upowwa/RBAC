package upowwa;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DateUtilsTest {

    @Test
    void getCurrentDate_shouldMatchPattern() {
        assertTrue(DateUtils.getCurrentDate().matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    void getCurrentDateTime_shouldMatchPattern() {
        assertTrue(DateUtils.getCurrentDateTime().matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    void isBefore_shouldReturnTrueForEarlierDate() {
        assertTrue(DateUtils.isBefore("2026-05-01", "2026-05-10"));
    }

    @Test
    void isAfter_shouldReturnTrueForLaterDate() {
        assertTrue(DateUtils.isAfter("2026-05-10", "2026-05-01"));
    }

    @Test
    void addDays_shouldAddDaysCorrectly() {
        assertEquals("2026-05-15", DateUtils.addDays("2026-05-10", 5));
    }

    @Test
    void formatRelativeTime_today() {
        assertEquals("today", DateUtils.formatRelativeTime(DateUtils.getCurrentDate()));
    }
}
