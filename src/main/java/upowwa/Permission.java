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

        //нормализация
        String normalizedName = name.trim().replaceAll("\\s+", "_").toUpperCase();
        String normalizedResource = resource.trim().toLowerCase();
        String normalizedDescription = description.trim();

        this.name = normalizedName;
        this.resource = normalizedResource;
        this.description = normalizedDescription;
    }

    public String format() {
        return name + " on " + resource + ": " + description;
    }

    public boolean matches(String namePattern, String resourcePattern) {
        return name.contains(namePattern) && resource.contains(resourcePattern);
    }
}
