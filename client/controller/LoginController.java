package client.controller;

import client.MainClient;
import client.network.ClientConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import shared.message.*;
import shared.model.User;

public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private ComboBox<String> roleBox;

    @FXML
    private Label errorLabel;

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
        try {
            String username = usernameField.getText();

            if (username.isBlank()) {
                errorLabel.setText("Username is required");
                return;
            }

            ClientConnection conn = ClientConnection.getInstance();
            conn.connect("localhost", 5000);

            User user = new User(username, roleBox.getValue());
            conn.send(new Message(type, user));

            Message response = conn.receive();

            if (response.getType() == MessageType.LOGIN_SUCCESS) {
                MainClient.showMain();
            } else if (response.getType() == MessageType.ERROR) {
                ErrorPayload err = (ErrorPayload) response.getPayload();
                errorLabel.setText(err.getMessage());
            }
        } catch (Exception e) {
            errorLabel.setText("Server not reachable");
        }
    }
}
