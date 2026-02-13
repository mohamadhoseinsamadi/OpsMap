package shared.model;

public class RectangleRegion extends RegionShape {
    private double x;
    private double y;
    private double width;
    private double height;

    public RectangleRegion(double x, double y, double width, double height, String color, String owner, String id) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
        this.owner = owner;
        this.id = id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }
}
