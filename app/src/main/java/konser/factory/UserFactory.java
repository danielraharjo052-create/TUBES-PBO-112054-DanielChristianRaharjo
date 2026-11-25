package konser.factory;

import konser.model.User;

public abstract class UserFactory {

    public abstract User createUser(int id, String username);

}
