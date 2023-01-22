package kurulus.display;

import java.awt.Graphics2D;

import kurulus.Kurulus;

import java.awt.Color;
import java.awt.Font;

public final class Renderer {
  private final Graphics2D graphics;
  private final Font       debugFont;

  Renderer(Graphics2D graphics) {
    this.graphics = graphics;
    debugFont     = new Font("Hack", Font.PLAIN, 10);
  }

  public void writeDebugText(float x, float y, String... lines) {
    graphics.setFont(debugFont);
    graphics.setColor(Color.WHITE);
    final var metrics = graphics.getFontMetrics();
    for (final var line : lines) {
      y += metrics.getHeight();
      graphics.drawString(line, x, y);
    }
  }

  public void clear() {
    graphics.clearRect(0, 0, Kurulus.WINDOW_WIDTH, Kurulus.WINDOW_HEIGHT);
  }
}
