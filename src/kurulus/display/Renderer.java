package kurulus.display;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;

import kurulus.Kurulus;

public final class Renderer {
  private final Graphics2D graphics;

  Renderer(Graphics2D graphics) { this.graphics = graphics; }

  public void write(float x, float y, Color foreground, Color background,
    Font font, String... lines) {
    graphics.setColor(foreground);
    graphics.setFont(font);
    final var metrics = graphics.getFontMetrics();
    for (final var line : lines) {
      if (background != null) {
        graphics.setColor(background);
        graphics.fillRect((int) (x - metrics.getHeight() * 0.05f + 0.5f),
          (int) (y + 0.5f),
          (int) (metrics.stringWidth(line) + metrics.getHeight() * 0.1f + 0.5f),
          metrics.getHeight());
        graphics.setColor(foreground);
      }
      y += metrics.getHeight();
      graphics.drawString(line, x, y - metrics.getDescent());
    }
  }

  public void drawImage(float x, float y, float scale, BufferedImage image) {
    graphics.drawImage(image, new AffineTransform(scale / image.getWidth(), 0f,
      0f, scale / image.getHeight(), x, y), null);
  }

  public void clear() {
    graphics.clearRect(0, 0, Kurulus.WINDOW_WIDTH, Kurulus.WINDOW_HEIGHT);
  }
}
