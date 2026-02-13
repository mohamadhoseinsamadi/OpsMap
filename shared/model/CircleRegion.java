package shared.model;

public class CircleRegion extends RegionShape {
    private double centerX;
    private double centerY;
    private double radius;

    public CircleRegion(double centerX, double centerY, double radius, String color, String owner, String id) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
        this.color = color;
        this.owner = owner;
        this.id = id;
    }

    public double getCenterX() {
        return centerX;
    }

    public double getCenterY() {
        return centerY;
    }

    public double getRadius() {
        return radius;
    }
}
