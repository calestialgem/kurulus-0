package kurulus.game;

import java.util.Map;

import kurulus.game.settlement.Settlement;
import kurulus.game.world.Area;
import kurulus.game.world.World;

public record Game(World world, Date date, Map<Area, Settlement> settlements) {
  public Game simulateToday() {
    return new Game(world, date.findNextDay(), settlements);
  }
}
