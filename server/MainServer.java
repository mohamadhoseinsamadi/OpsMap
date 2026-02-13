package server;

import server.manager.ClientManager;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MainServer {
    public static final int PORT = 5000;

    public static void main(String[] args) {
        ClientManager clientManager = new ClientManager();
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket, clientManager);
                handler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
