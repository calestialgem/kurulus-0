package kurulus.display;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JFrame;

import kurulus.Kurulus;

public final class Display {
  private final Canvas canvas;
  private final JFrame frame;

  public Display() {
    canvas = new Canvas();
    frame  = new JFrame();

    {
      final var dimension =
        new Dimension(Kurulus.WINDOW_WIDTH, Kurulus.WINDOW_HEIGHT);
      canvas.setMaximumSize(dimension);
      canvas.setMinimumSize(dimension);
      canvas.setPreferredSize(dimension);
    }

    frame.setTitle("Kuruluş %s".formatted(Kurulus.VERSION));
    frame.add(canvas);
    frame.pack();
    frame.setResizable(false);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
    frame.requestFocus();

    canvas.requestFocus();
    canvas.setBackground(Color.BLACK);
    canvas.createBufferStrategy(2);
  }

  public void draw() { canvas.getBufferStrategy().show(); }
  public void dispose() {
    canvas.getBufferStrategy().dispose();
    frame.dispose();
  }
  public Input createInput() { return new Input(frame, canvas); }
  public Renderer createRenderer() {
    final var graphics =
      (Graphics2D) canvas.getBufferStrategy().getDrawGraphics();
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
      RenderingHints.VALUE_ANTIALIAS_ON);
    graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
      RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
      RenderingHints.VALUE_RENDER_QUALITY);
    return new Renderer(graphics);
  }
}
