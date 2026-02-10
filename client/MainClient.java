package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainClient extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        showLogin();
    }

    public static void showLogin() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                MainClient.class.getResource("/client/ui/login.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setTitle("Ops Map - Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void showMain() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                MainClient.class.getResource("/client/ui/main.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setTitle("Ops Map - Operation Room");
        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
