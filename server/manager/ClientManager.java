package server.manager.ClientManager;

import server.ClientHandler;
import shared.message.Message;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ClientManager {
    private Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());

    public void addClient(ClientHandler client) {
        clients.add(client);
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    public void broadcast(Message msg, ClientHandler exclude) {
        synchronized (clients) {
            for (ClientHandler c : clients) {
                if (c != exclude) {
                    try {
                        c.send(msg);
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }
}
