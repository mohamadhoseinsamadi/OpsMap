package client.controller;

import client.MainClient;
import client.network.ClientConnection;
import client.network.MessageListener;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import shared.message.ErrorPayload;
import shared.message.Message;
import shared.message.MessageType;
import shared.model.*;

import java.io.File;
import java.util.*;

public class MainController implements MessageListener {
    @FXML private Pane mapPane;
    @FXML private ToggleButton routeToolBtn;
    @FXML private ToggleButton markerToolBtn;
    @FXML private ToggleButton regionToolBtn;
    @FXML private ChoiceBox<String> markerTypeBox;
    @FXML private ColorPicker colorPicker;
    @FXML private Slider thicknessSlider;
    @FXML private ChoiceBox<String> shapeTypeBox;
    @FXML private Button deleteSelectedBtn;
    @FXML private Button loadBgBtn;
    @FXML private ListView<String> chatList;
    @FXML private TextField chatInput;
    @FXML private ListView<String> userList;

    private Polyline currentLine;
    private Route currentRoute;
    private double startX, startY;
    private Shape tempShape;
    private final Map<String, Circle> cursors = new HashMap<>();
    private final Map<String, Marker> markers = new HashMap<>();
    private final Map<String, RegionShape> regions = new HashMap<>();
    private final List<Route> routes = new ArrayList<>();
    private User currentUser;
    private String selectedObjectId;

    @FXML
    public void initialize() {
        currentUser = MainClient.getCurrentUser();

        ToggleGroup tools = new ToggleGroup();
        routeToolBtn.setToggleGroup(tools);
        markerToolBtn.setToggleGroup(tools);
        regionToolBtn.setToggleGroup(tools);

        markerTypeBox.getItems().addAll("SAFE", "DANGER", "BASE");
        markerTypeBox.getSelectionModel().selectFirst();

        shapeTypeBox.getItems().addAll("Rectangle", "Circle");
        shapeTypeBox.getSelectionModel().selectFirst();

        thicknessSlider.setMin(1);
        thicknessSlider.setMax(10);
        thicknessSlider.setValue(3);
        thicknessSlider.setShowTickLabels(true);
        thicknessSlider.setShowTickMarks(true);

        colorPicker.setValue(Color.RED);

        setupRouteDrawing();
        setupMarker();
        setupRegion();
        setupMouseTracking();
        setupObjectSelection();

        ClientConnection.getInstance().startListening(this);
    }

    private void setupRouteDrawing() {
        mapPane.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            if (!routeToolBtn.isSelected()) return;
            currentLine = new Polyline();
            currentLine.setStroke(colorPicker.getValue());
            currentLine.setStrokeWidth(thicknessSlider.getValue());
            currentRoute = new Route(colorPicker.getValue().toString(), thicknessSlider.getValue(), currentUser.getUsername());
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
            routes.add(currentRoute);
            try {
                ClientConnection.getInstance().send(new Message(MessageType.DRAW_ROUTE, currentRoute));
            } catch (Exception ex) {
                showError("Failed to send route");
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
            if (!markerToolBtn.isSelected() || e.isSecondaryButtonDown()) return;
            Color color;
            switch (markerTypeBox.getValue()) {
                case "SAFE": color = Color.GREEN; break;
                case "DANGER": color = Color.RED; break;
                case "BASE": color = Color.BLUE; break;
                default: color = Color.BLACK;
            }
            Marker marker = new Marker(e.getX(), e.getY(), markerTypeBox.getValue(),
                    color.toString(), currentUser.getUsername(), UUID.randomUUID().toString());
            drawMarker(marker);
            try {
                ClientConnection.getInstance().send(new Message(MessageType.ADD_MARKER, marker));
            } catch (Exception ex) {
                showError("Failed to send marker");
            }
        });
    }

    private void setupRegion() {
        mapPane.setOnMousePressed(e -> {
            if (!regionToolBtn.isSelected()) return;
            startX = e.getX();
            startY = e.getY();
            String shape = shapeTypeBox.getValue();
            if ("Rectangle".equals(shape)) {
                Rectangle rect = new Rectangle();
                rect.setStroke(colorPicker.getValue());
                rect.setFill(Color.color(colorPicker.getValue().getRed(),
                        colorPicker.getValue().getGreen(),
                        colorPicker.getValue().getBlue(), 0.3));
                rect.setX(startX);
                rect.setY(startY);
                rect.setWidth(0);
                rect.setHeight(0);
                tempShape = rect;
            } else if ("Circle".equals(shape)) {
                Circle circle = new Circle();
                circle.setStroke(colorPicker.getValue());
                circle.setFill(Color.color(colorPicker.getValue().getRed(),
                        colorPicker.getValue().getGreen(),
                        colorPicker.getValue().getBlue(), 0.3));
                circle.setCenterX(startX);
                circle.setCenterY(startY);
                circle.setRadius(0);
                tempShape = circle;
            }
            if (tempShape != null) {
                mapPane.getChildren().add(tempShape);
            }
        });

        mapPane.setOnMouseDragged(e -> {
            if (tempShape == null) return;
            if (tempShape instanceof Rectangle) {
                Rectangle r = (Rectangle) tempShape;
                r.setX(Math.min(startX, e.getX()));
                r.setY(Math.min(startY, e.getY()));
                r.setWidth(Math.abs(e.getX() - startX));
                r.setHeight(Math.abs(e.getY() - startY));
            } else if (tempShape instanceof Circle) {
                Circle c = (Circle) tempShape;
                double dx = e.getX() - startX;
                double dy = e.getY() - startY;
                c.setRadius(Math.sqrt(dx*dx + dy*dy));
            }
        });

        mapPane.setOnMouseReleased(e -> {
            if (tempShape == null) return;
            RegionShape region = null;
            String id = UUID.randomUUID().toString();
            String colorStr = colorPicker.getValue().toString();
            if (tempShape instanceof Rectangle) {
                Rectangle r = (Rectangle) tempShape;
                region = new RectangleRegion(r.getX(), r.getY(), r.getWidth(), r.getHeight(),
                        colorStr, currentUser.getUsername(), id);
            } else if (tempShape instanceof Circle) {
                Circle c = (Circle) tempShape;
                region = new CircleRegion(c.getCenterX(), c.getCenterY(), c.getRadius(),
                        colorStr, currentUser.getUsername(), id);
            }
            if (region != null) {
                drawRegion(region);
                try {
                    ClientConnection.getInstance().send(new Message(MessageType.ADD_REGION, region));
                } catch (Exception ex) {
                    showError("Failed to send region");
                }
            }
            tempShape = null;
        });
    }

    private void setupMouseTracking() {
        mapPane.setOnMouseMoved(e -> {
            MousePosition pos = new MousePosition(e.getX(), e.getY(), currentUser.getUsername());
            try {
                ClientConnection.getInstance().send(new Message(MessageType.MOUSE_MOVE, pos));
            } catch (Exception ex) {}
        });
    }

    private void setupObjectSelection() {
        mapPane.setOnMouseClicked(e -> {
            if (e.isSecondaryButtonDown()) return;
            javafx.scene.Node picked = e.getPickResult().getIntersectedNode();
            if (picked != null && picked.getUserData() instanceof String) {
                selectedObjectId = (String) picked.getUserData();
            } else {
                selectedObjectId = null;
            }
        });
    }

    @FXML
    private void deleteSelected() {
        if (selectedObjectId == null) {
            showError("No object selected");
            return;
        }
        String owner = null;
        if (markers.containsKey(selectedObjectId)) owner = markers.get(selectedObjectId).getOwner();
        else if (regions.containsKey(selectedObjectId)) owner = regions.get(selectedObjectId).getOwner();
        if (owner != null && !canModify(owner)) {
            showError("You don't have permission to delete this object");
            return;
        }
        removeObject(selectedObjectId);
    }

    @FXML
    private void loadBackground() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Background Image");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif")
        );
        File file = fc.showOpenDialog(mapPane.getScene().getWindow());
        if (file != null) {
            try {
                Image img = new Image(file.toURI().toString());
                BackgroundImage bgImg = new BackgroundImage(img,
                        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.DEFAULT,
                        new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, false));
                mapPane.setBackground(new Background(bgImg));
            } catch (Exception e) {
                showError("Could not load image: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onSave() {
        MapState state = collectMapState();
        try {
            ClientConnection.getInstance().send(new Message(MessageType.SAVE_STATE, state));
        } catch (Exception ex) {
            showError("Failed to send save request");
        }
    }

    @FXML
    private void onLoad() {
        try {
            ClientConnection.getInstance().send(new Message(MessageType.LOAD_STATE, null));
        } catch (Exception ex) {
            showError("Failed to send load request");
        }
    }

    @FXML
    private void sendChat() {
        String text = chatInput.getText();
        if (text.isBlank()) return;
        text = text.replaceAll("<[^>]*>", "").trim();
        if (text.length() > 200) text = text.substring(0, 200);
        ChatMessage msg = new ChatMessage(currentUser.getUsername(), text);
        try {
            ClientConnection.getInstance().send(new Message(MessageType.CHAT, msg));
        } catch (Exception ex) {
            showError("Failed to send chat");
        }
        chatInput.clear();
    }

    @Override
    public void onMessage(Message msg) {
        Platform.runLater(() -> {
            try {
                switch (msg.getType()) {
                    case ADD_MARKER:
                        drawMarker((Marker) msg.getPayload());
                        break;
                    case ADD_REGION:
                        drawRegion((RegionShape) msg.getPayload());
                        break;
                    case REMOVE_OBJECT:
                        removeById((String) msg.getPayload());
                        break;
                    case DRAW_ROUTE:
                        drawRemoteRoute((Route) msg.getPayload());
                        break;
                    case MOUSE_MOVE:
                        showRemoteCursor((MousePosition) msg.getPayload());
                        break;
                    case CHAT:
                        ChatMessage cm = (ChatMessage) msg.getPayload();
                        chatList.getItems().add(cm.user + ": " + cm.text);
                        chatList.scrollTo(chatList.getItems().size() - 1);
                        break;
                    case MAP_STATE:
                        loadMap((MapState) msg.getPayload());
                        break;
                    case USER_LIST:
                        updateUserList((List<User>) msg.getPayload());
                        break;
                    case ERROR:
                        ErrorPayload err = (ErrorPayload) msg.getPayload();
                        showError(err.toString());
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                showError("Error processing message: " + e.getMessage());
            }
        });
    }

    private void drawMarker(Marker m) {
        Circle c = new Circle(m.getX(), m.getY(), 6);
        c.setFill(Color.web(m.getColor()));
        c.setUserData(m.getId());
        markers.put(m.getId(), m);
        c.setOnMouseClicked(e -> {
            if (e.isSecondaryButtonDown() && canModify(m.getOwner())) {
                removeObject(m.getId());
            }
            selectedObjectId = m.getId();
        });
        mapPane.getChildren().add(c);
    }

    private void drawRegion(RegionShape region) {
        Shape shape = null;
        if (region instanceof RectangleRegion) {
            RectangleRegion r = (RectangleRegion) region;
            Rectangle rect = new Rectangle(r.getX(), r.getY(), r.getWidth(), r.getHeight());
            rect.setStroke(Color.web(r.getColor()));
            rect.setFill(Color.color(Color.web(r.getColor()).getRed(),
                    Color.web(r.getColor()).getGreen(),
                    Color.web(r.getColor()).getBlue(), 0.3));
            shape = rect;
        } else if (region instanceof CircleRegion) {
            CircleRegion cr = (CircleRegion) region;
            Circle circle = new Circle(cr.getCenterX(), cr.getCenterY(), cr.getRadius());
            circle.setStroke(Color.web(cr.getColor()));
            circle.setFill(Color.color(Color.web(cr.getColor()).getRed(),
                    Color.web(cr.getColor()).getGreen(),
                    Color.web(cr.getColor()).getBlue(), 0.3));
            shape = circle;
        }
        if (shape != null) {
            shape.setUserData(region.getId());
            regions.put(region.getId(), region);
            shape.setOnMouseClicked(e -> {
                if (e.isSecondaryButtonDown() && canModify(region.getOwner())) {
                    removeObject(region.getId());
                }
                selectedObjectId = region.getId();
            });
            mapPane.getChildren().add(shape);
        }
    }

    private void drawRemoteRoute(Route route) {
        Polyline line = new Polyline();
        for (int i = 0; i < route.getXPoints().size(); i++) {
            line.getPoints().addAll(route.getXPoints().get(i), route.getYPoints().get(i));
        }
        line.setStroke(Color.web(route.getColor()));
        line.setStrokeWidth(route.getThickness());
        routes.add(route);
        mapPane.getChildren().add(line);
    }

    private void showRemoteCursor(MousePosition pos) {
        if (pos.user.equals(currentUser.getUsername())) return;
        Circle c = cursors.get(pos.user);
        if (c == null) {
            c = new Circle(5, Color.ORANGE);
            cursors.put(pos.user, c);
            mapPane.getChildren().add(c);
        }
        c.setCenterX(pos.x);
        c.setCenterY(pos.y);
    }

    private void removeObject(String id) {
        try {
            ClientConnection.getInstance().send(new Message(MessageType.REMOVE_OBJECT, id));
        } catch (Exception ex) {
            showError("Failed to send delete request");
        }
        removeById(id);
    }

    private void removeById(String id) {
        markers.remove(id);
        regions.remove(id);
        mapPane.getChildren().removeIf(n -> id.equals(n.getUserData()));
        if (id.equals(selectedObjectId)) selectedObjectId = null;
    }

    private MapState collectMapState() {
        MapState state = new MapState();
        routes.forEach(state::addRoute);
        markers.values().forEach(state::addMarker);
        regions.values().forEach(state::addRegion);
        return state;
    }

    private void loadMap(MapState state) {
        mapPane.getChildren().clear();
        cursors.clear();
        markers.clear();
        regions.clear();
        routes.clear();
        selectedObjectId = null;
        state.getRoutes().forEach(this::drawRemoteRoute);
        state.getMarkers().forEach(this::drawMarker);
        state.getRegions().forEach(this::drawRegion);
    }

    private void updateUserList(List<User> users) {
        List<String> names = new ArrayList<>();
        for (User u : users) {
            names.add(u.getUsername() + " (" + u.getRole() + ")");
        }
        userList.setItems(FXCollections.observableArrayList(names));
    }

    private boolean canModify(String owner) {
        return currentUser.getRole().equals("Commander") || currentUser.getUsername().equals(owner);
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
            alert.show();
        });
    }
}
