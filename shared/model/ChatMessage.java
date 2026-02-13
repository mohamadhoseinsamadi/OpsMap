package shared.model;

import java.io.Serializable;

public class ChatMessage implements Serializable {
    public String user;
    public String text;

    public ChatMessage(String user, String text) {
        this.user = user;
        this.text = text;
    }
}
