package server.manager;

import shared.model.MapState;
import shared.model.Route;
import shared.model.Marker;
import shared.model.RegionShape;
import java.util.Iterator;

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

    public synchronized boolean removeObject(String id) {
        boolean removed = false;

        Iterator<Marker> markerIt = currentState.getMarkers().iterator();
        while (markerIt.hasNext()) {
            if (markerIt.next().getId().equals(id)) {
                markerIt.remove();
                removed = true;
                break;
            }
        }


        Iterator<RegionShape> regionIt = currentState.getRegions().iterator();
        while (regionIt.hasNext()) {
            if (regionIt.next().getId().equals(id)) {
                regionIt.remove();
                removed = true;
                break;
            }
        }

        Iterator<Route> routeIt = currentState.getRoutes().iterator();
        while (routeIt.hasNext()) {
            if (routeIt.next().getId().equals(id)) {
                routeIt.remove();
                removed = true;
                break;
            }
        }

        return removed;
    }
}