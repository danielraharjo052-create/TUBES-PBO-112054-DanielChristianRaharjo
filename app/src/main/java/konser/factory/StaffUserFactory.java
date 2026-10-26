package konser.factory;

import konser.model.StaffUser;

import konser.model.User;

public class StaffUserFactory extends UserFactory {

    @Override public User createUser(int id, String username) {
        return new StaffUser(id, username);
    }

}
