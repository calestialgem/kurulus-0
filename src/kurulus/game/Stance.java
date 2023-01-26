package kurulus.game;

import java.awt.Color;

public record Stance(String name, Color color) {
  public static final Stance SELF = new Stance("Self", new Color(58, 229, 77));
  public static final Stance ALLY = new Stance("Ally", new Color(33, 146, 255));
  public static final Stance NEUTRAL =
    new Stance("Neutral", new Color(255, 222, 0));
  public static final Stance ENEMY =
    new Stance("Enemy", new Color(255, 50, 50));
}
