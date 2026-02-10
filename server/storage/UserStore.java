package server.storage.UserStore;

import java.util.HashSet;
import java.util.Set;

public class UserStore {
    private Set<String> users = new HashSet<>();

    public boolean userExists(String username) {
        return users.contains(username);
    }

    public void addUser(String username) {
        users.add(username);
    }
}