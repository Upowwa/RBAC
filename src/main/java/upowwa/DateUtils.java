package upowwa;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtils {
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String getCurrentDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(DATETIME_FORMATTER);
    }

    public static boolean isBefore(String date1, String date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("Даты не могут быть null");
        }
        String d1 = normalizeDate(date1);
        String d2 = normalizeDate(date2);
        return d1.compareTo(d2) < 0;
    }

    public static boolean isAfter(String date1, String date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("Даты не могут быть null");
        }
        String d1 = normalizeDate(date1);
        String d2 = normalizeDate(date2);
        return d1.compareTo(d2) > 0;
    }

    public static boolean isEqual(String date1, String date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return normalizeDate(date1).equals(normalizeDate(date2));
    }

    public static String addDays(String date, int days) {
        if (date == null || date.trim().isEmpty()) {
            throw new IllegalArgumentException("Дата не может быть пустой");
        }
        String trimmed = date.trim();

        try {
            if (trimmed.length() == 10) {
                LocalDate localDate = LocalDate.parse(trimmed, DATE_FORMATTER);
                return localDate.plusDays(days).format(DATE_FORMATTER);
            } else if (trimmed.length() == 19) {
                LocalDateTime localDateTime = LocalDateTime.parse(trimmed, DATETIME_FORMATTER);
                return localDateTime.plusDays(days).format(DATETIME_FORMATTER);
            } else if (trimmed.length() == 16) {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                LocalDateTime localDateTime = LocalDateTime.parse(trimmed, fmt);
                return localDateTime.plusDays(days).format(fmt);
            } else {
                throw new IllegalArgumentException("Неподдерживаемый формат даты: " + date);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка обработки даты: " + date, e);
        }
    }

    public static String formatRelativeTime(String date) {
        if (date == null || date.trim().isEmpty()) {
            return "unknown";
        }
        try {
            String normalized = normalizeDate(date);
            LocalDate targetDate = LocalDate.parse(normalized, DATE_FORMATTER);
            LocalDate today = LocalDate.now();

            long days = ChronoUnit.DAYS.between(today, targetDate);

            if (days == 0) {
                return "today";
            } else if (days == 1) {
                return "tomorrow";
            } else if (days == -1) {
                return "yesterday";
            } else if (days > 0) {
                return "in " + days + " days";
            } else {
                return Math.abs(days) + " days ago";
            }
        } catch (Exception e) {
            return "unknown";
        }
    }

    private static String normalizeDate(String date) {
        if (date == null) {
            throw new IllegalArgumentException("Дата не может быть null");
        }
        String trimmed = date.trim();

        if (trimmed.length() == 10) {
            return trimmed;
        }

        if (trimmed.length() >= 10) {
            return trimmed.substring(0, 10);
        }
        throw new IllegalArgumentException("Неподдерживаемый формат даты: " + date);
    }

    public static boolean isValidDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            return false;
        }
        try {
            String normalized = normalizeDate(date);
            LocalDate.parse(normalized, DATE_FORMATTER);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValidDateTime(String dateTime) {
        if (dateTime == null || dateTime.trim().isEmpty()) {
            return false;
        }
        try {
            String trimmed = dateTime.trim();
            if (trimmed.length() == 10) {
                LocalDate.parse(trimmed, DATE_FORMATTER);
            } else if (trimmed.length() == 19) {
                LocalDateTime.parse(trimmed, DATETIME_FORMATTER);
            } else if (trimmed.length() == 16) {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                LocalDateTime.parse(trimmed, fmt);
            } else {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}