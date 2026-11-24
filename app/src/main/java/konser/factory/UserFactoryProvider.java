package konser.factory;

public final class UserFactoryProvider {

    private UserFactoryProvider() {
    }

    public static UserFactory forRole(String role) {

        switch (role.toUpperCase()) {

            case "ADMIN":
                return new AdminUserFactory();

            case "STAFF":
                return new StaffUserFactory();

            case "CUSTOMER":
                return new CustomerUserFactory();

            default:
                throw new IllegalArgumentException(
                    "Role pengguna tidak dikenal: " + role
                );
        }
    }
}