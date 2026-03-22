package upowwa;

public record User(String username, String fullName, String email) {

    public User {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username не может быть null или пустым");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name не может быть null или пустым");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email не может быть null или пустым");
        }
    }

    public static User create(String username, String fullName, String email) {
        String cleanUsername = username.trim();
        if (cleanUsername.length() < 3 || cleanUsername.length() > 20) {
            throw new IllegalArgumentException("Username должен быть от 3 до 20 символов");
        }
        if (!cleanUsername.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Username может содержать только латинские буквы, цифры и подчёркивание");
        }
        if (!email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("Email должен содержать @ и точку после @");
        }
        return new User(cleanUsername, fullName.trim(), email.trim());
    }

    public String format() {
        return username + " (" + fullName + ")";
    }
}
