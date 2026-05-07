package upowwa;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ValidationUtilsTest {

    @ParameterizedTest
    @ValueSource(strings = {"admin", "john_doe", "user123"})
    void isValidUsername_valid(String username) {
        assertTrue(ValidationUtils.isValidUsername(username));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "ab", "user name", "user@name"})
    void isValidUsername_invalid(String username) {
        assertFalse(ValidationUtils.isValidUsername(username));
    }

    @ParameterizedTest
    @ValueSource(strings = {"admin@mail.com", "john.smith@test.org"})
    void isValidEmail_valid(String email) {
        assertTrue(ValidationUtils.isValidEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {"badmail", "test@", "@mail.com"})
    void isValidEmail_invalid(String email) {
        assertFalse(ValidationUtils.isValidEmail(email));
    }

    @Test
    void normalizeString_shouldTrimAndNormalizeSpaces() {
        String input = "  John   Smith  ";
        String expected = "John Smith";  // Title Case как в оригинале

        String result = ValidationUtils.normalizeString(input);

        assertEquals(expected, result, "Должна убрать лишние пробелы, сохранив регистр");
    }

    @Test
    void requireNonEmpty_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.requireNonEmpty("   ", "username"));
    }
}