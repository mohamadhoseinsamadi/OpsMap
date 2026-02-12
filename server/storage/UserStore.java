package server.storage;

import java.util.HashMap;
import java.util.Map;

public class UserStore {
    private static class UserRecord {
        String username;
        String password;
        String role;

        UserRecord(String username, String password, String role) {
            this.username = username;
            this.password = password;
            this.role = role;
        }
    }

    private Map<String, UserRecord> users = new HashMap<>();

    public boolean userExists(String username) {
        return users.containsKey(username);
    }

    public boolean validate(String username, String password) {
        UserRecord r = users.get(username);
        if (r == null) return false;
        return r.password.equals(password);
    }

    public void addUser(String username, String password, String role) {
        users.put(username, new UserRecord(username, password, role));
    }

   
    public String getRole(String username) {
        UserRecord r = users.get(username);
        return r != null ? r.role : null;
    }
}
