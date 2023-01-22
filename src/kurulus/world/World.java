package kurulus.world;

import kurulus.Kurulus;

public final class World {
  private final Area[][] area;

  public World() {
    area = new Area[Kurulus.WORLD_SIZE][Kurulus.WORLD_SIZE];

    for (var x = 0; x < Kurulus.WORLD_SIZE; x++) {
      for (var y = 0; y < Kurulus.WORLD_SIZE; y++) {
        area[x][y] = new Area(x, y);
      }
    }
  }

  public int getWidth() { return area[0].length; }
  public int getHeight() { return area.length; }
}
