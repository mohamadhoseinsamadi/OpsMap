package shared.model;
import java.io.Serializable;
public abstract class RegionShape implements Serializable {
    protected String id;
    protected String color;
    protected String owner;
public String getId() {
    return id;
}

public String getColor() {
    return color;
}

public String getOwner() {
    return owner;
}

}
