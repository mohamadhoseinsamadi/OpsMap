package server;

import server.auth.AuthManager;
import server.manager.ClientManager;
import server.manager.MapStateManager;
import server.storage.MapStorage;
import shared.message.ErrorPayload;
import shared.message.Message;
import shared.message.MessageType;
import shared.model.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private ClientManager clientManager;
    private User user;

    public ClientHandler(Socket socket, ClientManager manager) {
        this.socket = socket;
        this.clientManager = manager;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            while (true) {
                Message message = (Message) in.readObject();
                handleMessage(message);
            }
        } catch (Exception e) {

        } finally {
            try {
                if (user != null) {
                    clientManager.removeClient(this);
                    clientManager.broadcast(new Message(MessageType.USER_LEFT, user), this);
                    clientManager.broadcast(new Message(MessageType.USER_LIST, clientManager.getUsers()), null);
                }
                socket.close();
            } catch (Exception e) {

            }
        }
    }

    private void handleMessage(Message msg) throws IOException {
        try {

            if (msg.getType() != MessageType.LOGIN &&
                    msg.getType() != MessageType.REGISTER &&
                    user == null) {
                sendError("UNAUTHORIZED", "You must be logged in to perform this action.");
                return;
            }

            switch (msg.getType()) {
                case LOGIN:
                    handleLogin(msg);
                    break;
                case REGISTER:
                    handleRegister(msg);
                    break;
                case DRAW_ROUTE:
                    Route route = (Route) msg.getPayload();
                    MapStateManager.getInstance().addRoute(route);
                    clientManager.broadcast(msg, this);
                    break;
                case ADD_MARKER:
                    Marker marker = (Marker) msg.getPayload();
                    MapStateManager.getInstance().addMarker(marker);
                    clientManager.broadcast(msg, this);
                    break;
                case ADD_REGION:
                    RegionShape region = (RegionShape) msg.getPayload();
                    MapStateManager.getInstance().addRegion(region);
                    clientManager.broadcast(msg, this);
                    break;
                case REMOVE_OBJECT:
                    handleRemoveObject(msg);
                    break;
                case MOUSE_MOVE:
                case CHAT:
                    clientManager.broadcast(msg, this);
                    break;
                case SAVE_STATE:
                    try {
                        MapStorage.save((MapState) msg.getPayload());
                    } catch (Exception e) {
                        sendError("SAVE_FAILED", "Could not save map: " + e.getMessage());
                    }
                    break;
                case LOAD_STATE:
                    handleLoadState();
                    break;
                default:
                    clientManager.broadcast(msg, this);
                    break;
            }
        } catch (Exception e) {
            sendError("SERVER_ERROR", "Error processing request: " + e.getMessage());
        }
    }


    private void handleRemoveObject(Message msg) throws IOException {
        String id = (String) msg.getPayload();
        MapState state = MapStateManager.getInstance().getCurrentState();
        String owner = findOwnerById(id, state);

        if (owner == null) {
            sendError("NOT_FOUND", "Object with id " + id + " does not exist.");
            return;
        }

        if (!user.getUsername().equals(owner) && !"Commander".equals(user.getRole())) {
            sendError("FORBIDDEN", "You don't have permission to delete this object.");
            return;
        }

        boolean removed = MapStateManager.getInstance().removeObject(id);
        if (removed) {
            clientManager.broadcast(msg, this);
        } else {
            sendError("REMOVE_FAILED", "Could not remove object (already deleted?)");
        }
    }


    private void handleLoadState() throws IOException {
        try {
            MapState loaded = MapStorage.load();
            MapStateManager.getInstance().setCurrentState(loaded);
            send(new Message(MessageType.MAP_STATE, loaded));
            clientManager.broadcast(new Message(MessageType.MAP_STATE, loaded), null);
        } catch (Exception e) {
            sendError("LOAD_FAILED", "Could not load map: " + e.getMessage());
        }
    }


    private String findOwnerById(String id, MapState state) {
        for (Marker m : state.getMarkers()) {
            if (m.getId().equals(id)) return m.getOwner();
        }
        for (RegionShape r : state.getRegions()) {
            if (r.getId().equals(id)) return r.getOwner();
        }
        for (Route r : state.getRoutes()) {
            if (r.getId().equals(id)) return r.getOwner();
        }
        return null;
    }

    private void handleLogin(Message msg) throws IOException {
        AuthManager auth = AuthManager.getInstance();
        User u = (User) msg.getPayload();

        String username = u.getUsername() != null ? u.getUsername().trim() : "";
        String password = u.getPassword() != null ? u.getPassword().trim() : "";

        // اعتبارسنجی ورودی
        if (username.length() < 3 || username.length() > 20) {
            sendError("LOGIN_INVALID", "Username must be 3-20 characters");
            return;
        }
        if (password.length() < 4) {
            sendError("LOGIN_INVALID", "Password must be at least 4 characters");
            return;
        }

        User authenticated = auth.login(username, password);
        if (authenticated != null) {
            this.user = authenticated;
            clientManager.addClient(this);
            send(new Message(MessageType.LOGIN_SUCCESS, this.user));
            send(new Message(MessageType.MAP_STATE, MapStateManager.getInstance().getCurrentState()));
            clientManager.broadcast(new Message(MessageType.USER_JOINED, this.user), this);
            clientManager.broadcast(new Message(MessageType.USER_LIST, clientManager.getUsers()), null);
        } else {
            sendError("LOGIN_FAILED", "Invalid username or password");
        }
    }

    private void handleRegister(Message msg) throws IOException {
        AuthManager auth = AuthManager.getInstance();
        User u = (User) msg.getPayload();
        String username = u.getUsername() != null ? u.getUsername().trim() : "";
        String password = u.getPassword() != null ? u.getPassword().trim() : "";
        String role = u.getRole();

        if (username.length() < 3 || username.length() > 20) {
            sendError("REGISTER_INVALID", "Username must be 3-20 characters");
            return;
        }
        if (password.length() < 4) {
            sendError("REGISTER_INVALID", "Password must be at least 4 characters");
            return;
        }

        if (!"Commander".equals(role) && !"Operator".equals(role)) {
            sendError("REGISTER_INVALID", "Role must be Commander or Operator");
            return;
        }

        if (auth.register(username, password, role)) {

            User authenticated = auth.login(username, password);
            this.user = authenticated;
            clientManager.addClient(this);
            send(new Message(MessageType.LOGIN_SUCCESS, this.user));
            send(new Message(MessageType.MAP_STATE, MapStateManager.getInstance().getCurrentState()));
            clientManager.broadcast(new Message(MessageType.USER_JOINED, this.user), this);
            clientManager.broadcast(new Message(MessageType.USER_LIST, clientManager.getUsers()), null);
        } else {
            sendError("REGISTER_FAILED", "Username already exists");
        }
    }

    public void send(Message msg) throws IOException {
        out.writeObject(msg);
        out.flush();
    }

    private void sendError(String code, String text) throws IOException {
        ErrorPayload err = new ErrorPayload(code, text);
        send(new Message(MessageType.ERROR, err));
    }

    public User getUser() {
        return user;
    }
}
