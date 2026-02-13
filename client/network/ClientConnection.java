package client.network;

import shared.message.Message;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientConnection {
    private static ClientConnection instance = new ClientConnection();
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket socket;

    private ClientConnection() {}

    public static ClientConnection getInstance() {
        return instance;
    }

    public void connect(String host, int port) throws Exception {
        socket = new Socket(host, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    public void send(Message msg) throws Exception {
        out.writeObject(msg);
        out.flush();
    }

    public Message receive() throws Exception {
        return (Message) in.readObject();
    }

    public void startListening(MessageListener listener) {
        new Thread(() -> {
            try {
                while (true) {
                    Message msg = receive();
                    listener.onMessage(msg);
                }
            } catch (Exception e) {
                // connection closed
            }
        }).start();
    }
}
