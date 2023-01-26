package kurulus.userinterface;

import kurulus.Vector;

public record Rectangle(Vector topLeft, Vector bottomRight) {
  public Rectangle() { this(new Vector(), new Vector()); }

  public Rectangle roundOutwards() {
    return new Rectangle(topLeft.floor(), bottomRight.ceil());
  }

  public Rectangle intersect(Rectangle other) {
    return new Rectangle(topLeft.max(other.topLeft),
      bottomRight.min(other.bottomRight));
  }

  public boolean findIntersection(Vector point) {
    return point.x() > topLeft.x() && point.y() > topLeft.y()
      && point.x() < bottomRight.x() && point.y() < bottomRight.y();
  }
}
