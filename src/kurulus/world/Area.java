package kurulus.world;

public final class Area {
  public final int     x;
  public final int     y;
  public final double  altitude;
  public final Terrain terrain;

  Area(int x, int y, double altitude, Terrain terrain) {
    this.x        = x;
    this.y        = y;
    this.altitude = altitude;
    this.terrain  = terrain;
  }
}
