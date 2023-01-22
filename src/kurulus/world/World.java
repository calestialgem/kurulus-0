package kurulus.world;

import java.awt.Color;

import kurulus.Kurulus;

public final class World {
  private final Area[][] areas;
  public final Terrain   plains;
  public final Terrain   mountains;
  public final Terrain   seas;

  public World() {
    areas     = new Area[Kurulus.WORLD_SIZE][Kurulus.WORLD_SIZE];
    plains    = new Terrain(Color.GREEN);
    mountains = new Terrain(Color.RED.darker());
    seas      = new Terrain(Color.BLUE.darker());

    for (var x = 0; x < Kurulus.WORLD_SIZE; x++) {
      for (var y = 0; y < Kurulus.WORLD_SIZE; y++) {
        areas[x][y] = new Area(x, y, Math.random() > 0.75 ? mountains
          : Math.random() > 0.75 ? seas : plains);
      }
    }
  }

  public Area getArea(int x, int y) { return areas[x][y]; }
  public int getWidth() { return areas[0].length; }
  public int getHeight() { return areas.length; }
}
