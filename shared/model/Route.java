package shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Route implements Serializable {
    private List<Double> xPoints = new ArrayList<>();
    private List<Double> yPoints = new ArrayList<>();
    private String color;
    private double thickness;
    private String owner;

    public Route(String color, double thickness, String owner) {
        this.color = color;
        this.thickness = thickness;
        this.owner = owner;
    }

    public void addPoint(double x, double y) {
        xPoints.add(x);
        yPoints.add(y);
    }

    public List<Double> getXPoints() {
        return xPoints;
    }

    public List<Double> getYPoints() {
        return yPoints;
    }

    public String getColor() {
        return color;
    }

    public double getThickness() {
        return thickness;
    }

    public String getOwner() {
        return owner;
    }
}
