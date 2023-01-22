package kurulus.world;

public final class Area {
  public final int x;
  public final int y;

  public Terrain terrain;

  Area(int x, int y, Terrain terrain) {
    this.x       = x;
    this.y       = y;
    this.terrain = terrain;
  }
}
