package client.controller;

import client.MainClient;
import client.network.ClientConnection;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import shared.message.ErrorPayload;
import shared.message.Message;
import shared.message.MessageType;
import shared.model.User;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleBox;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        roleBox.getItems().addAll("Commander", "Operator");
        roleBox.getSelectionModel().selectFirst();
    }

    @FXML
    private void onLogin() {
        authenticate(MessageType.LOGIN);
    }

    @FXML
    private void onRegister() {
        authenticate(MessageType.REGISTER);
    }

    private void authenticate(MessageType type) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String role = roleBox.getValue();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Username and password are required");
            return;
        }
        if (username.length() < 3 || username.length() > 20) {
            errorLabel.setText("Username must be 3-20 characters");
            return;
        }
        if (password.length() < 4) {
            errorLabel.setText("Password must be at least 4 characters");
            return;
        }

        try {
            ClientConnection conn = ClientConnection.getInstance();
            conn.connect("localhost", 5000);
            User user = new User(username, role, password);
            conn.send(new Message(type, user));
            Message response = conn.receive();
            if (response.getType() == MessageType.LOGIN_SUCCESS) {
                MainClient.setCurrentUser((User) response.getPayload());
                MainClient.showMain();
            } else if (response.getType() == MessageType.ERROR) {
                ErrorPayload err = (ErrorPayload) response.getPayload();
                errorLabel.setText(err.getMessage());
            }
        } catch (Exception e) {
            errorLabel.setText("Cannot connect to server");
        }
    }
}
