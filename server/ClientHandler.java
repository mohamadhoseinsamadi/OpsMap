package server.ClientHandler;

import server.auth.AuthManager;
import server.manager.ClientManager;
import server.storage.MapStorage;
import shared.message.*;
import shared.model.User;
import shared.model.MapState;
import java.io.*;
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
            System.out.println("Client disconnected");
        } finally {
            clientManager.removeClient(this);
        }
    }

    private void handleMessage(Message msg) throws IOException {
        switch (msg.getType()) {
            case LOGIN -> handleLogin(msg);
            case REGISTER -> handleRegister(msg);
            case SAVE_STATE -> {
                MapStorage.save((MapState) msg.getPayload());
            }
            case LOAD_STATE -> {
                try {
                    MapState state = MapStorage.load();
                    send(new Message(MessageType.MAP_STATE, state));
                } catch (Exception e) {
                    sendError("LOAD_ERROR", "Failed to load map");
                }
            }
            default -> {
                clientManager.broadcast(msg, this);
            }
        }
    }

    private void handleLogin(Message msg) throws IOException {
        AuthManager auth = AuthManager.getInstance();
        User u = (User) msg.getPayload();

        if (auth.login(u.getUsername())) {
            this.user = u;
            clientManager.addClient(this);
            send(new Message(MessageType.LOGIN_SUCCESS, u));
            clientManager.broadcast(new Message(MessageType.USER_JOINED, u), this);
        } else {
            sendError("LOGIN_FAILED", "Invalid username");
        }
    }

    private void handleRegister(Message msg) throws IOException {
        AuthManager auth = AuthManager.getInstance();
        User u = (User) msg.getPayload();

        if (auth.register(u.getUsername())) {
            send(new Message(MessageType.LOGIN_SUCCESS, u));
        } else {
            sendError("REGISTER_FAILED", "User already exists");
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
