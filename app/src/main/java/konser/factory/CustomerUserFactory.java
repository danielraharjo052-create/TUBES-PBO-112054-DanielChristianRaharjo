package konser.factory;

import konser.model.CustomerUser;

import konser.model.User;

public class CustomerUserFactory extends UserFactory {

    @Override public User createUser(int id, String username) {
        return new CustomerUser(id, username);
    }

}
