package upowwa;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleUtilsTest {

    @Test
    void promptString_shouldReturnInput() {
        Scanner scanner = new Scanner(new ByteArrayInputStream("hello\n".getBytes()));
        String result = ConsoleUtils.promptString(scanner, "Введите строку", true);
        assertEquals("hello", result);
    }

    @Test
    void promptInt_shouldRetryUntilValid() {
        Scanner scanner = new Scanner(new ByteArrayInputStream("abc\n0\n3\n".getBytes()));
        int result = ConsoleUtils.promptInt(scanner, "Введите число", 1, 5);
        assertEquals(3, result);
    }

    @Test
    void promptYesNo_shouldReturnTrueForYes() {
        Scanner scanner = new Scanner(new ByteArrayInputStream("да\n".getBytes()));
        boolean result = ConsoleUtils.promptYesNo(scanner, "Подтверждение");
        assertTrue(result);
    }

    @Test
    void promptChoice_shouldReturnSelectedOption() {
        Scanner scanner = new Scanner(new ByteArrayInputStream("2\n".getBytes()));
        String result = ConsoleUtils.promptChoice(scanner, "Выбор", List.of("A", "B", "C"));
        assertEquals("B", result);
    }
}