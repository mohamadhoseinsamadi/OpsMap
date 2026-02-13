package shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MapState implements Serializable {
    private List<Route> routes = new ArrayList<>();
    private List<Marker> markers = new ArrayList<>();
    private List<RegionShape> regions = new ArrayList<>();

    public List<Route> getRoutes() {
        return routes;
    }

    public List<Marker> getMarkers() {
        return markers;
    }

    public List<RegionShape> getRegions() {
        return regions;
    }

    public void addRoute(Route route) {
        routes.add(route);
    }

    public void addMarker(Marker marker) {
        markers.add(marker);
    }

    public void addRegion(RegionShape region) {
        regions.add(region);
    }
}
