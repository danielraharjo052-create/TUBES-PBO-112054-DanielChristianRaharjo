package konser.controller;

import konser.exception.SecurityException;

import konser.model.User;

import konser.service.AuthService;

public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service=service;
    }

public User login(String username,String password) throws SecurityException {
    return service.login(username,password);
}

public void registerCustomer(String username, String password) throws SecurityException {
    service.registerCustomer(username, password);
}

}
