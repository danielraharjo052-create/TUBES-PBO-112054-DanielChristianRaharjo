package konser.factory;

import konser.model.User;

// DESIGN PATTERN 3: Factory Method. Subclass menentukan jenis User yang dibuat.
public abstract class UserFactory {

    public abstract User createUser(int id, String username);

}
