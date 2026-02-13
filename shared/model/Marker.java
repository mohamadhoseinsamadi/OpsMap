package shared.model;

import java.io.Serializable;

public class Marker implements Serializable {
    private double x;
    private double y;
    private String type;
    private String color;
    private String owner;
    private String id;

    public Marker(double x, double y, String type, String color, String owner, String id) {
        this.x = x;
        this.y = y;
        this.type = type;
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

    public String getType() {
        return type;
    }

    public String getColor() {
        return color;
    }

    public String getOwner() {
        return owner;
    }

    public String getId() {
        return id;
    }
}
