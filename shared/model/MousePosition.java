package shared.model;

import java.io.Serializable;

public class MousePosition implements Serializable {
    public double x;
    public double y;
    public String user;

    public MousePosition(double x, double y, String user) {
        this.x = x;
        this.y = y;
        this.user = user;
    }
}
