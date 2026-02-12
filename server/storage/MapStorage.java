package server.storage;

import shared.model.MapState;
import java.io.*;

public class MapStorage {
    private static final String FILE = "map_state.dat";

    public static void save(MapState state) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE))) {
            out.writeObject(state);
        }
    }

    public static MapState load() throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(FILE))) {
            return (MapState) in.readObject();
        }
    }
}

