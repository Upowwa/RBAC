package upowwa;

public record Permission(String name, String resource, String description) {

    public Permission(String name, String resource, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name не может быть null или пустым");
        }
        if (resource == null || resource.trim().isEmpty()) {
            throw new IllegalArgumentException("Resource не может быть null или пустым");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description не может быть пустым");
        }

        name = name.trim();
        resource = resource.trim();
        description = description.trim();

        if (name.contains(" ")) {
            throw new IllegalArgumentException("Name не должно содержать пробелов");
        }

        this.name = name.toUpperCase();
        this.resource = resource.toLowerCase();
        this.description = description;
    }

    public String format() {
        return name + " on " + resource + ": " + description;
    }

    public boolean matches(String namePattern, String resourcePattern) {
        if (namePattern == null || resourcePattern == null) {
            return false;
        }
        return name.contains(namePattern.trim().toUpperCase())
                && resource.contains(resourcePattern.trim().toLowerCase());
    }
}