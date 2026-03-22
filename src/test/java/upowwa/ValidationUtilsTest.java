package upowwa;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ValidationUtilsTest {

    @Test
    void testIsValidUsername() {
        assertTrue(ValidationUtils.isValidUsername("john123"));
        assertFalse(ValidationUtils.isValidUsername("ab"));
        assertFalse(ValidationUtils.isValidUsername("john@"));
    }

    @Test
    void testIsValidEmail() {
        assertTrue(ValidationUtils.isValidEmail("user@example.com"));
        assertFalse(ValidationUtils.isValidEmail("invalid"));
    }

    @Test
    void testNormalizeString() {
        assertEquals("Hello WORLD", ValidationUtils.normalizeString("  Hello   WORLD  "));
        assertEquals("hello world", ValidationUtils.normalizeStringLower("  Hello   WORLD  "));
        assertEquals("HELLO WORLD", ValidationUtils.normalizeStringUpper("  Hello   WORLD  "));
    }

    @Test
    void testRequireNonEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.requireNonEmpty("", "field"));
    }

    @Test
    void testIsValidDate_REAL() {
        // ТЕСТЫ ПОД ТВОЮ РЕАЛИЗАЦИЮ LocalDateTime форматтеров
        assertFalse(ValidationUtils.isValidDate("2026-03-23"));  // ❌ Только LocalDateTime!
        assertFalse(ValidationUtils.isValidDate("invalid"));
        assertTrue(ValidationUtils.isValidDate("2026-03-23 12:00")); // ✅ LocalDateTime
    }
}
