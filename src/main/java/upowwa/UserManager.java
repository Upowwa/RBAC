package upowwa;

import java.util.*;
import java.util.stream.Collectors;

public class UserManager implements Repository<User> {
    private final Map<String, User> users = new HashMap<>();

    @Override
    public void add(User user) {
        validateUser(user);
        if (users.containsKey(user.username())) {
            throw new IllegalArgumentException("Пользователь с username '" + user.username() + "' уже существует");
        }
        users.put(user.username(), user);
    }

    @Override
    public boolean remove(User user) {
        return users.remove(user.username()) != null;
    }

    @Override
    public Optional<User> findById(String id) {
        return users.values().stream()
                .filter(u -> u.username().equals(id))
                .findFirst();
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public int count() {
        return users.size();
    }

    @Override
    public void clear() {
        users.clear();
    }

    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(users.get(username));
    }

    public Optional<User> findByEmail(String email) {
        return users.values().stream()
                .filter(u -> u.email().equals(email))
                .findFirst();
    }

    public List<User> findByFilter(UserFilter filter) {
        return users.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<User> findAll(UserFilter filter, Comparator<User> sorter) {
        return users.values().stream()
                .filter(filter::test)
                .sorted(sorter)
                .collect(Collectors.toList());
    }

    public boolean exists(String username) {
        return users.containsKey(username);
    }

    public void update(String username, String newFullName, String newEmail) {
        User user = users.get(username);
        if (user == null) {
            throw new IllegalArgumentException("Пользователь '" + username + "' не найден");
        }

        //валидация нового email
        if (users.values().stream().anyMatch(u -> !u.username().equals(username) && u.email().equals(newEmail))) {
            throw new IllegalArgumentException("Email '" + newEmail + "' уже используется");
        }

        //создание обновленного пользователя
        User updatedUser = User.create(username, newFullName, newEmail);
        users.put(username, updatedUser);
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User не может быть null");
        }
    }

    public User findByName(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        return findByUsername(username).orElse(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserManager that = (UserManager) o;
        return users.equals(that.users);
    }

    @Override
    public int hashCode() {
        return Objects.hash(users);
    }
}
