package kurulus.world;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import kurulus.Kurulus;

public final class World {
  private final Area[][]  areas;
  private final Terrain[] terrains;

  public World() {
    areas    = new Area[Kurulus.WORLD_SIZE][Kurulus.WORLD_SIZE];
    terrains = new Terrain[] { new Terrain(Color.RED.darker()),
      new Terrain(Color.YELLOW.darker()), new Terrain(Color.GREEN.darker()),
      new Terrain(Color.YELLOW.brighter()), new Terrain(Color.BLUE.brighter()),
      new Terrain(Color.BLUE), new Terrain(Color.BLUE.darker()) };

    final var rng = new Random();

    final var nuclei      =
      Kurulus.WORLD_SIZE * Kurulus.WORLD_SIZE * Kurulus.NUCLEI_FRACTION;
    final var activeAreas = new HashSet<Area>();

    for (var i = 0; i < nuclei; i++) {
      int x = 0;
      int y = 0;
      do {
        x = rng.nextInt(getWidth());
        y = rng.nextInt(getHeight());
      } while (areas[x][y] != null);

      final var altitude = rng.nextDouble(Kurulus.MIN_NUCLEUS_ALTITUDE,
        Kurulus.MAX_NUCLEUS_ALTITUDE);
      final var nucleus  = new Area(x, y, altitude, getTerrain(altitude));
      areas[x][y] = nucleus;
      activeAreas.add(nucleus);
    }

    final var nextBatch = new HashSet<Area>();
    while (!activeAreas.isEmpty()) {
      nextBatch.clear();

      for (final var active : activeAreas) {
        for (final var neighbor : findNeighbors(active)) {
          if (neighbor.terrain != null) { continue; }
          var altitude = 0d;
          var isActive = false;

          final var surroundings = findNeighbors(neighbor);
          for (final var surrounding : surroundings) {
            if (surrounding.terrain == null) {
              isActive = true;
              continue;
            }
            altitude += surrounding.altitude
              + rng.nextDouble(-Kurulus.ALTITUDE_DROP_BALANCE, 1)
                / Kurulus.ALTITUDE_DROP_BALANCE
                * Kurulus.ALTITUDE_DROP_MAGNITUDE;
          }
          altitude /= surroundings.size();

          final var area =
            new Area(neighbor.x, neighbor.y, altitude, getTerrain(altitude));
          areas[area.x][area.y] = area;
          if (isActive) { nextBatch.add(area); }
        }
      }

      activeAreas.clear();
      activeAreas.addAll(nextBatch);
    }
  }

  public Area getArea(int x, int y) { return areas[x][y]; }
  public int getWidth() { return areas[0].length; }
  public int getHeight() { return areas.length; }

  private List<Area> findNeighbors(Area a) {
    final var neighbors = new ArrayList<Area>();
    if (a.x > 0) { addNonNull(neighbors, a.x - 1, a.y); }
    if (a.y > 0) { addNonNull(neighbors, a.x, a.y - 1); }
    if (a.x < getWidth() - 1) { addNonNull(neighbors, a.x + 1, a.y); }
    if (a.y < getHeight() - 1) { addNonNull(neighbors, a.x, a.y + 1); }
    return neighbors;
  }

  private void addNonNull(List<Area> list, int x, int y) {
    list.add(areas[x][y] != null ? areas[x][y] : new Area(x, y, 0, null));
  }

  private Terrain getTerrain(double altitude) {
    for (int i = 0; i < Kurulus.TERRAIN_ALTITUDE_BOUNDARIES.length; i++) {
      if (altitude >= Kurulus.TERRAIN_ALTITUDE_BOUNDARIES[i]) {
        return terrains[i];
      }
    }
    return terrains[terrains.length - 1];
  }
}
