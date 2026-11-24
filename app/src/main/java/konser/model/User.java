package konser.model;

public abstract class User extends Entity {

    private final String username;

    private final String role;

    protected User(int id, String username, String role) {

        super(id);

        this.username = username;

        this.role = role;

    }

public String getUsername() {
    return username;
}

public String getRole() {
    return role;
}

}
