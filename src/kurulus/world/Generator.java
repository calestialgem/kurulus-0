package kurulus.world;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import kurulus.Vector;

public record Generator(Vector size, Terrain[] terrains,
  double[] altitudeBoundaries, double nucleiFraction, double minNucleusAltitude,
  double maxNucleusAltitude, double altitudeDropBalance,
  double altitudeDropMagnitude) {
  public World generate(Random rng) {
    final var areas        = new Area[size.getX()][size.getY()];
    final var altitudes    = new double[size.getX()][size.getY()];
    final var nucleusCount = size.x() * size.y() * nucleiFraction;

    var activeAreas = new HashSet<Area>();

    for (var i = 0; i < nucleusCount; i++) {
      final var coordinate = findEmptyCoordinate(areas, rng);
      final var altitude   =
        rng.nextDouble(minNucleusAltitude, maxNucleusAltitude);
      final var nucleus    = new Area(coordinate, convertToTerrain(altitude));
      setArea(areas, nucleus);
      setAltitude(altitudes, coordinate, altitude);
      activeAreas.add(nucleus);
    }

    while (!activeAreas.isEmpty()) {
      final var nextBatch = new HashSet<Area>();

      for (final var active : activeAreas) {
        for (final var neighbor : findNeighbors(areas, active.coordinate())
          .entrySet()) {
          if (neighbor.getValue().isPresent()) { continue; }
          var altitude = 0d;
          var isActive = false;

          final var surroundings = findNeighbors(areas, neighbor.getKey());
          for (final var surrounding : surroundings.entrySet()) {
            if (surrounding.getValue().isEmpty()) {
              isActive = true;
              continue;
            }

            altitude += getAltitude(altitudes, surrounding.getKey())
              + rng.nextDouble(-altitudeDropBalance, 1) / altitudeDropBalance
                * altitudeDropMagnitude;
          }

          altitude /= surroundings.size();
          setAltitude(altitudes, neighbor.getKey(), altitude);

          final var area =
            new Area(neighbor.getKey(), convertToTerrain(altitude));
          setArea(areas, area);
          if (isActive) { nextBatch.add(area); }
        }
      }

      activeAreas = nextBatch;
    }

    return new World(areas);
  }

  private Vector findEmptyCoordinate(Area[][] areas, Random rng) {
    while (true) {
      final var coordinate =
        new Vector(rng.nextFloat(), rng.nextFloat()).mul(size).floor();
      if (getArea(areas, coordinate) == null) { return coordinate; }
    }
  }

  private Area getArea(Area[][] areas, Vector coordinate) {
    return areas[coordinate.getX()][coordinate.getY()];
  }
  private void setArea(Area[][] areas, Area area) {
    areas[area.coordinate().getX()][area.coordinate().getY()] = area;
  }

  private double getAltitude(double[][] altitudes, Vector coordinate) {
    return altitudes[coordinate.getX()][(int) (coordinate.y() + 0.5f)];
  }
  private void setAltitude(double[][] altitudes, Vector coordinate,
    double altitude) {
    altitudes[coordinate.getX()][coordinate.getY()] = altitude;
  }

  private Terrain convertToTerrain(double altitude) {
    for (var i = 0; i < altitudeBoundaries.length; i++) {
      if (altitude >= altitudeBoundaries[i]) { return terrains[i]; }
    }
    return terrains[terrains.length - 1];
  }

  private Map<Vector, Optional<Area>> findNeighbors(Area[][] areas,
    Vector coordinate) {
    final var neighbors = new HashMap<Vector, Optional<Area>>();
    if (coordinate.x() > 0) {
      putOptionalArea(areas, neighbors, coordinate.subX(1));
    }
    if (coordinate.y() > 0) {
      putOptionalArea(areas, neighbors, coordinate.subY(1));
    }
    if (coordinate.x() < size.x() - 1) {
      putOptionalArea(areas, neighbors, coordinate.addX(1));
    }
    if (coordinate.y() < size.y() - 1) {
      putOptionalArea(areas, neighbors, coordinate.addY(1));
    }
    return neighbors;
  }

  private void putOptionalArea(Area[][] areas, Map<Vector, Optional<Area>> map,
    Vector coordinate) {
    map.put(coordinate, getOptionalArea(areas, coordinate));
  }

  private Optional<Area> getOptionalArea(Area[][] areas, Vector coordinate) {
    final var area = getArea(areas, coordinate);
    if (area == null) { return Optional.empty(); }
    return Optional.of(area);
  }
}
