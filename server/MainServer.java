package server.MainServer;

import server.manager.ClientManager;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MainServer {
    public static final int PORT = 5000;

    public static void main(String[] args) {
        System.out.println("Ops Map Server starting...");
        ClientManager clientManager = new ClientManager();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");
                ClientHandler handler = new ClientHandler(socket, clientManager);
                handler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
