package kurulus.display;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferStrategy;

import kurulus.Kurulus;

public final class Renderer {
  public static record HorizontalAlignment(float shiftRatio) {
    public static final HorizontalAlignment LEFT = new HorizontalAlignment(0);
    public static final HorizontalAlignment RIGHT = new HorizontalAlignment(1);
    public static final HorizontalAlignment CENTER =
      new HorizontalAlignment(0.5f);
  }

  static Renderer init(BufferStrategy bufferStrategy) {
    final var renderer =
      new Renderer((Graphics2D) bufferStrategy.getDrawGraphics());

    renderer.graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
      RenderingHints.VALUE_ANTIALIAS_ON);
    renderer.graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
      RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    renderer.graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
      RenderingHints.VALUE_RENDER_QUALITY);

    return renderer;
  }

  private final Graphics2D graphics;

  private Renderer(Graphics2D graphics) { this.graphics = graphics; }

  public float getHeight(Font font) {
    graphics.setFont(font);
    return graphics.getFontMetrics().getHeight();
  }

  public void write(float x, float y, Color foreground, Color background,
    Font font, String... lines) {
    write(x, y, foreground, background, font, HorizontalAlignment.LEFT, lines);
  }

  public void write(float x, float y, Color foreground, Color background,
    Font font, HorizontalAlignment alignment, String... lines) {
    graphics.setColor(foreground);
    graphics.setFont(font);
    final var metrics = graphics.getFontMetrics();
    for (final var line : lines) {
      y = writeLine(x - metrics.stringWidth(line) * alignment.shiftRatio(), y,
        foreground, background, metrics, line);
    }
  }

  private float writeLine(float x, float y, Color foreground, Color background,
    FontMetrics metrics, String line) {
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
    return y;
  }

  public void drawImage(float x, float y, float scale, BufferedImage image) {
    graphics.drawImage(image, new AffineTransform(scale / image.getWidth(), 0f,
      0f, scale / image.getHeight(), x, y), null);
  }

  public void fillSquare(float x, float y, float size, Color color) {
    graphics.setColor(color);
    graphics.fillRect((int) (x + 0.5f), (int) (y + 0.5f), (int) (size + 0.5f),
      (int) (size + 0.5f));
  }

  public void fillCircle(float x, float y, float size, Color color) {
    graphics.setColor(color);
    graphics.fillOval((int) (x + 0.5f), (int) (y + 0.5f), (int) (size + 0.5f),
      (int) (size + 0.5f));
  }

  public void drawSquare(float x, float y, float size, Stroke stroke,
    Color color) {
    graphics.setColor(color);
    graphics.setStroke(stroke);
    graphics.drawRect((int) (x + 0.5f), (int) (y + 0.5f), (int) (size + 0.5f),
      (int) (size + 0.5f));
  }

  public void drawLine(float startX, float startY, float endX, float endY,
    Stroke stroke, Color color) {
    graphics.setColor(color);
    graphics.setStroke(stroke);
    graphics.drawLine((int) (startX + 0.5f), (int) (startY + 0.5f),
      (int) (endX + 0.5f), (int) (endY + 0.5f));
  }

  public void clear() {
    graphics.clearRect(0, 0, Kurulus.WINDOW_WIDTH, Kurulus.WINDOW_HEIGHT);
  }
}
