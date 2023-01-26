package kurulus.game;

import java.util.HashMap;
import java.util.Map;

import kurulus.game.world.Area;
import kurulus.game.world.World;

public final class Game {
  public final World                 world;
  public final Map<Area, Settlement> settlements;

  private Date date;

  public Game(World world) {
    this.world = world;

    settlements = new HashMap<>();

    date = new Date(1, 1, 2200);
  }

  public void simulateToday() { date = date.findNextDay(); }

  public boolean settle(Area area) {
    if (!area.terrain().land() || settlements.containsKey(area)) {
      return false;
    }
    settlements.put(area, new Settlement(area));
    return true;
  }

  public Date getDate() { return date; }
}
