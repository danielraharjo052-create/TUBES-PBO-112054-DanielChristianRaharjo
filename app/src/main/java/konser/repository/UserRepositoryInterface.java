package konser.repository;

import konser.model.User;
import java.sql.SQLException;
import java.util.Optional;

public interface UserRepositoryInterface {
    Optional<User> authenticate(String username, String password) throws SQLException;
    void createUser(String username, String password, String role) throws SQLException;
}
