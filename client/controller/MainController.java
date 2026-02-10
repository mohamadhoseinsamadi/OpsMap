package client.controller;

import client.network.ClientConnection;
import client.network.MessageListener;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import shared.message.Message;
import shared.message.MessageType;
import shared.model.*;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

public class MainController implements MessageListener {
    @FXML
    private Pane mapPane;
    @FXML
    private ToggleButton routeToolBtn;
    @FXML
    private ToggleButton markerToolBtn;
    @FXML
    private ToggleButton regionToolBtn;
    @FXML
    private ChoiceBox<String> markerTypeBox;
    @FXML
    private ListView<String> chatList;
    @FXML
    private TextField chatInput;

    private Polyline currentLine;
    private Route currentRoute;
    private double startX, startY;
    private Rectangle tempRect;
    private Map<String, Circle> cursors = new HashMap<>();

    @FXML
    public void initialize() {
        markerTypeBox.getItems().addAll("SAFE", "DANGER", "BASE");
        markerTypeBox.getSelectionModel().selectFirst();

        setupRouteDrawing();
        setupMarker();
        setupRegion();
        setupMouseTracking();
        ClientConnection.getInstance().startListening(this);
    }

    private void setupRouteDrawing() {
        mapPane.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            if (!routeToolBtn.isSelected()) return;

            currentLine = new Polyline();
            currentLine.setStroke(Color.RED);
            currentLine.setStrokeWidth(3);

            currentRoute = new Route("RED", 3, "localUser");

            addPoint(e);
            mapPane.getChildren().add(currentLine);
        });

        mapPane.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            if (currentLine == null) return;
            addPoint(e);
        });

        mapPane.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            if (currentLine == null) return;

            addPoint(e);

            try {
                ClientConnection.getInstance()
                    .send(new Message(MessageType.DRAW_ROUTE, currentRoute));
            } catch (Exception ex) {
                System.out.println("Failed to send route");
            }

            currentLine = null;
            currentRoute = null;
        });
    }

    private void addPoint(MouseEvent e) {
        currentLine.getPoints().addAll(e.getX(), e.getY());
        currentRoute.addPoint(e.getX(), e.getY());
    }

    private void setupMarker() {
        mapPane.setOnMouseClicked(e -> {
            if (!markerToolBtn.isSelected()) return;

            Marker marker = new Marker(
                e.getX(),
                e.getY(),
                markerTypeBox.getValue(),
                "BLUE",
                "localUser",
                UUID.randomUUID().toString()
            );

            drawMarker(marker);

            try {
                ClientConnection.getInstance()
                    .send(new Message(MessageType.ADD_MARKER, marker));
            } catch (Exception ignored) {}
        });
    }

    private void setupRegion() {
        mapPane.setOnMousePressed(e -> {
            if (!regionToolBtn.isSelected()) return;

            startX = e.getX();
            startY = e.getY();

            tempRect = new Rectangle();
            tempRect.setStroke(Color.GREEN);
            tempRect.setFill(Color.color(0,1,0,0.3));

            mapPane.getChildren().add(tempRect);
        });

        mapPane.setOnMouseDragged(e -> {
            if (tempRect == null) return;

            tempRect.setX(Math.min(startX, e.getX()));
            tempRect.setY(Math.min(startY, e.getY()));
            tempRect.setWidth(Math.abs(e.getX() - startX));
            tempRect.setHeight(Math.abs(e.getY() - startY));
        });

        mapPane.setOnMouseReleased(e -> {
            if (tempRect == null) return;

            RectangleRegion region = new RectangleRegion(
                tempRect.getX(),
                tempRect.getY(),
                tempRect.getWidth(),
                tempRect.getHeight(),
                "GREEN",
                "localUser",
                UUID.randomUUID().toString()
            );

            try {
                ClientConnection.getInstance()
                    .send(new Message(MessageType.ADD_REGION, region));
            } catch (Exception ignored) {}

            tempRect = null;
        });
    }

    private void setupMouseTracking() {
        mapPane.setOnMouseMoved(e -> {
            MousePosition pos =
                new MousePosition(e.getX(), e.getY(), "localUser");

            try {
                ClientConnection.getInstance()
                    .send(new Message(MessageType.MOUSE_MOVE, pos));
            } catch (Exception ignored) {}
        });
    }

    @Override
    public void onMessage(Message msg) {
        Platform.runLater(() -> {
            switch (msg.getType()) {
                case ADD_MARKER -> drawMarker((Marker) msg.getPayload());
                case ADD_REGION -> drawRegion((RectangleRegion) msg.getPayload());
                case REMOVE_OBJECT -> removeById((String) msg.getPayload());
                case DRAW_ROUTE -> drawRemoteRoute((Route) msg.getPayload());
                case MOUSE_MOVE -> showRemoteCursor((MousePosition) msg.getPayload());
                case CHAT -> {
                    ChatMessage cm = (ChatMessage) msg.getPayload();
                    chatList.getItems().add(cm.user + ": " + cm.text);
                }
                case MAP_STATE -> loadMap((MapState) msg.getPayload());
            }
        });
    }

    private void drawMarker(Marker m) {
        Circle c = new Circle(m.getX(), m.getY(), 6);
        c.setFill(Color.valueOf(m.getColor()));
        c.setUserData(m.getId());

        c.setOnMouseClicked(e -> {
            if (e.isSecondaryButtonDown() && m.getOwner().equals("localUser")) {
                removeObject(m.getId());
            }
        });

        mapPane.getChildren().add(c);
    }

    private void drawRegion(RectangleRegion region) {
        Rectangle rect = new Rectangle(
            region.getX(),
            region.getY(),
            region.getWidth(),
            region.getHeight()
        );
        rect.setStroke(Color.valueOf(region.getColor()));
        rect.setFill(Color.color(0,1,0,0.3));
        rect.setUserData(region.getId());

        rect.setOnMouseClicked(e -> {
            if (e.isSecondaryButtonDown() && region.getOwner().equals("localUser")) {
                removeObject(region.getId());
            }
        });

        mapPane.getChildren().add(rect);
    }

    private void drawRemoteRoute(Route route) {
        Polyline line = new Polyline();

        for (int i = 0; i < route.getXPoints().size(); i++) {
            line.getPoints().addAll(
                route.getXPoints().get(i),
                route.getYPoints().get(i)
            );
        }

        line.setStroke(Color.valueOf(route.getColor()));
        line.setStrokeWidth(route.getThickness());

        mapPane.getChildren().add(line);
    }

    private void showRemoteCursor(MousePosition pos) {
        Circle c = cursors.get(pos.user);

        if (c == null) {
            c = new Circle(4, Color.ORANGE);
            cursors.put(pos.user, c);
            mapPane.getChildren().add(c);
        }

        c.setCenterX(pos.x);
        c.setCenterY(pos.y);
    }

    @FXML
    private void onSave() {
        MapState state = collectMapState();
        try {
            ClientConnection.getInstance()
                .send(new Message(MessageType.SAVE_STATE, state));
        } catch (Exception ignored) {}
    }

    @FXML
    private void onLoad() {
        try {
            ClientConnection.getInstance()
                .send(new Message(MessageType.LOAD_STATE, null));
        } catch (Exception ignored) {}
    }

    @FXML
    private void sendChat() {
        String txt = chatInput.getText();
        chatInput.clear();

        ChatMessage msg = new ChatMessage("localUser", txt);

        try {
            ClientConnection.getInstance()
                .send(new Message(MessageType.CHAT, msg));
        } catch (Exception ignored) {}
    }

    private void removeObject(String id) {
        try {
            ClientConnection.getInstance()
                .send(new Message(MessageType.REMOVE_OBJECT, id));
        } catch (Exception ignored) {}
    }

    private void removeById(String id) {
        mapPane.getChildren()
            .removeIf(n -> id.equals(n.getUserData()));
    }

    private MapState collectMapState() {
        MapState state = new MapState();
        // Note: در پیاده‌سازی واقعی باید اشیاء روی mapPane را جمع‌آوری کنید
        return state;
    }

    private void loadMap(MapState state) {
        mapPane.getChildren().clear();

        state.getRoutes().forEach(this::drawRemoteRoute);
        state.getMarkers().forEach(this::drawMarker);
        state.getRegions().forEach(this::drawRegion);
    }
}
