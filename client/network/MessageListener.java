package client.network;

import shared.message.Message;

public interface MessageListener {
    void onMessage(Message message);
}
