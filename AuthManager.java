package server.auth;

import server.storage.UserStore;

public class AuthManager {
    private static AuthManager instance = new AuthManager();
    private UserStore store = new UserStore();

    private AuthManager() {
    }

    public static AuthManager getInstance() {
        return instance;
    }

    public boolean login(String username) {
        return store.userExists(username);
    }

    public boolean register(String username) {
        if (store.userExists(username))
            return false;
        store.addUser(username);
        return true;
    }
}
