package server.storage.MapStorage;

import shared.model.MapState;
import java.io.*;

public class MapStorage {
    private static final String FILE = "map_state.dat";

    public static void save(MapState state) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE));
        out.writeObject(state);
        out.close();
    }

    public static MapState load() throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(FILE));
        MapState state = (MapState) in.readObject();
        in.close();
        return state;
    }
}
