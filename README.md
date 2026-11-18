# OpsMap — Collaborative Operations Map

A real-time collaborative mapping system: multiple users work on a shared operational map simultaneously, drawing routes, placing markers, defining regions, and chatting. Built as a Java client/server application with a JavaFX GUI.

## Architecture

- **`server/`** — user management, authentication, map state, and message broadcasting between clients.
- **`client/`** — JavaFX graphical interface: login/signup, map display, and drawing tools.
- **`shared/`** — models and message types shared between client and server.

## Features

- **User signup & login** with `Commander` and `Operator` roles (in-memory authentication).
- **Route drawing** with configurable color and stroke width.
- **Markers** of various types and colors.
- **Regions** defined as rectangles or circles.
- **Object deletion** restricted to the owner or a `Commander`.
- **Live cursor tracking** — see other users' mouse positions on the map.
- **Text chat** alongside the map.
- **Save/load map state** persisted on the server.
- **Error handling** with structured `ErrorPayload` messages sent to clients.

## Requirements

- Java 17 or later
- JavaFX SDK matching your JDK

## Running

1. Open the project in IntelliJ IDEA (or another Java IDE) and add the JavaFX SDK to the project libraries.
2. Run the server: `server.MainServer`.
3. Run the client: `client.MainClient`. Launch several client instances to simulate multiple users.

Clients connect to `localhost:5000` by default. To change the address/port, edit `LoginController` (client side) or `MainServer` (server side).

## Project Structure

```
client/
  MainClient.java        # JavaFX entry point
  controller/            # Login and main map controllers
  network/               # Server connection and message listener
  ui/                    # FXML layouts
server/
  MainServer.java        # Server entry point
  ClientHandler.java     # Per-client connection handler
  auth/                  # Authentication
  manager/               # Client and map state management
  storage/               # User store and map persistence
shared/
  message/               # Message, MessageType, ErrorPayload
  model/                 # User, Route, Marker, regions, MapState, ...
```

## License

MIT — see [LICENSE](LICENSE).