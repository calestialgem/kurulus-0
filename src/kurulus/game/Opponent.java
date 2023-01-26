package kurulus.game;

import kurulus.Vector;

public final class Opponent {
  private State controlled;

  Opponent(State controlled) { this.controlled = controlled; }

  public void update(Game game) {
    game.settle(new Vector(game.rng.nextInt(game.world.getWidth()),
      game.rng.nextInt(game.world.getHeight())), controlled);
  }
}
