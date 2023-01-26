package kurulus.game;

import kurulus.Vector;

public record World(Area[][] areas) {
  public Area getArea(Vector coordinate) {
    return getArea(coordinate.getX(), coordinate.getY());
  }
  public Area getArea(int x, int y) { return areas[x][y]; }

  public Vector getSize() { return new Vector(getWidth(), getHeight()); }
  public int getWidth() { return areas[0].length; }
  public int getHeight() { return areas.length; }
}
