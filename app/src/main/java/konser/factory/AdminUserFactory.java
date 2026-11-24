package konser.factory;

import konser.model.AdminUser;

import konser.model.User;

public class AdminUserFactory extends UserFactory {

    @Override public User createUser(int id, String username) {
        return new AdminUser(id, username);
    }

}
