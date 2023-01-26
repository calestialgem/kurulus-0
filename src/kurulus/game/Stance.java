package kurulus.game;

import java.awt.Color;

public record Stance(String name, Color color) {
  public static final Stance SELF = new Stance("Self", new Color(3, 201, 136));
  public static final Stance ALLY = new Stance("Ally", new Color(0, 119, 192));
  public static final Stance NEUTRAL =
    new Stance("Neutral", new Color(255, 222, 0));
  public static final Stance ENEMY = new Stance("Enemy", new Color(210, 0, 25));
}
