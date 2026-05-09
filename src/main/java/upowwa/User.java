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

        username = username.trim();
        fullName = fullName.trim();
        email = email.trim();

        if (username.length() < 3 || username.length() > 20) {
            throw new IllegalArgumentException("Username должен быть от 3 до 20 символов");
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Username может содержать только латинские буквы, цифры и подчёркивание");
        }

        int atIndex = email.indexOf('@');
        int dotIndex = email.lastIndexOf('.');

        if (atIndex <= 0 || dotIndex <= atIndex + 1 || dotIndex == email.length() - 1) {
            throw new IllegalArgumentException("Email должен содержать @ и точку после @");
        }
    }

    public static User create(String username, String fullName, String email) {
        return new User(username, fullName, email);
    }

    public String format() {
        return username + " (" + fullName + ")";
    }
}