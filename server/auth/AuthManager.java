package server.auth;

import server.storage.UserStore;
import shared.model.User;

public class AuthManager {
    private static AuthManager instance = new AuthManager();
    private UserStore store = new UserStore();

    private AuthManager() {}

    public static AuthManager getInstance() {
        return instance;
    }

    /**
     * Validates credentials and returns a User with the stored role if successful,
     * otherwise returns null.
     */
    public User login(String username, String password) {
        if (!store.validate(username, password)) {
            return null;
        }
        String role = store.getRole(username);
        if (role == null) {
            return null;
        }
        // Do not expose password back to clients
        return new User(username, role, "");
    }

    public boolean register(String username, String password, String role) {
        if (store.userExists(username)) {
            return false;
        }
        store.addUser(username, password, role);
        return true;
    }
}
