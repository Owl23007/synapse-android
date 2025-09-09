package top.contins.synapse.service;

import javax.inject.Inject;

public class AuthService {
    @Inject
    public AuthService() {}

    public boolean login(String email, String password) {
        return email != null && password != null && password.length() >= 6;
    }
}
