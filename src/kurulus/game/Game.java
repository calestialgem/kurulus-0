package kurulus.game;

import kurulus.game.world.World;

public record Game(World world, Date date) {
  public Game simulateToday() { return new Game(world, date.findNextDay()); }
}
