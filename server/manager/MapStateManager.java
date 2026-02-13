package server.manager;

import shared.model.MapState;
import shared.model.Route;
import shared.model.Marker;
import shared.model.RegionShape;

public class MapStateManager {
    private static MapStateManager instance = new MapStateManager();
    private MapState currentState = new MapState();

    private MapStateManager() {}

    public static MapStateManager getInstance() {
        return instance;
    }

    public MapState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(MapState state) {
        this.currentState = state;
    }

    public void addRoute(Route route) {
        currentState.addRoute(route);
    }

    public void addMarker(Marker marker) {
        currentState.addMarker(marker);
    }

    public void addRegion(RegionShape region) {
        currentState.addRegion(region);
    }

    public void removeObject(String id) {
        currentState.getMarkers().removeIf(m -> m.getId().equals(id));
        currentState.getRegions().removeIf(r -> r.getId().equals(id));
        // Routes don't have id, we ignore removal for simplicity
    }
}
