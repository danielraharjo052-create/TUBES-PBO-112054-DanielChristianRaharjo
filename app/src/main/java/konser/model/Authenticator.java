package konser.model;

import konser.exception.SecurityException;

import konser.model.User;

public interface Authenticator {

    User login(String username, String password) throws SecurityException;

}
