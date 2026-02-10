# OpsMap - Collaborative Operations Map

A real-time collaborative mapping application for operational management and coordination.


## 🚀 Features (Implemented in Full Project)

### Real-time Collaboration
- Multiple users can connect simultaneously
- Live drawing synchronization
- Real-time cursor tracking
- Instant chat messaging

### Mapping Tools
- **Routes**: Draw paths with customizable colors/thickness
- **Markers**: Place symbols (safe/danger/base) on map
- **Regions**: Define rectangular areas for operations
- **Ownership**: Users can only delete their own objects

### User Management
- Login/Register system
- Role-based access (Commander/Operator)
- User session management
- Online user list

### Data Management
- Save/Load entire map state
- Persistent user storage
- Error handling and recovery

## 🔧 Technology Stack

### Backend
- **Java Socket Programming** - For client-server communication
- **Multi-threading** - Handles multiple clients concurrently
- **Object Serialization** - For data transfer between client/server

### Frontend
- **JavaFX** - Modern UI framework
- **FXML** - Declarative UI design
- **MVC Pattern** - Clean architecture separation

### Shared Components
- **Custom Protocol** - Message-based communication
- **Serializable Models** - Cross-platform data transfer
- **Error Handling** - Structured error messages

## 📋 Prerequisites (for Full Implementation)

- Java JDK 11 or higher
- JavaFX SDK 11+
- IDE: IntelliJ IDEA, Eclipse, or VS Code

## 🎯 Project Purpose

This project demonstrates a complete real-time collaborative application with:
- Professional architecture design
- Clean code separation (client/server/shared)
- Scalable multi-user support
- Robust error handling
- Persistent data storage

## 🔄 Communication Protocol

The application uses a custom message-based protocol:
Message Types:

LOGIN, REGISTER, LOGIN_SUCCESS, LOGIN_FAILED

DRAW_ROUTE, ADD_MARKER, ADD_REGION, REMOVE_OBJECT

MOUSE_MOVE, USER_JOINED, USER_LEFT

CHAT, ERROR, SAVE_STATE, LOAD_STATE, MAP_STATE

text

## 📊 Design Patterns Used

1. **Singleton** - AuthManager, ClientConnection
2. **Observer** - MessageListener for real-time updates
3. **Factory** - Message creation
4. **MVC** - Client architecture
5. **Thread-per-Client** - Server architecture

## 👥 Team & Collaboration Features

- Real-time object synchronization
- Live user presence indicators
- Collaborative chat system
- Shared map editing
- User activity tracking

## 📝 Note

*This repository contains only the project structure and file organization. The actual implementation code is not included as it is part of an academic project.*

## 📄 License

Educational Project - University Assignment

---
**Advanced Programming Course Project**  

