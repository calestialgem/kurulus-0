package kurulus.display;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;

public final class Display {
  public static Display init(int width, int height, String title,
    Color background) {
    final var display = new Display(new Canvas(), new JFrame());

    final var dimension = new Dimension(width, height);
    display.canvas.setMaximumSize(dimension);
    display.canvas.setMinimumSize(dimension);
    display.canvas.setPreferredSize(dimension);

    display.frame.setTitle(title);
    display.frame.add(display.canvas);
    display.frame.pack();
    display.frame.setResizable(false);
    display.frame.setLocationRelativeTo(null);
    display.frame.setVisible(true);
    display.frame.requestFocus();

    display.canvas.requestFocus();
    display.canvas.setBackground(background);
    display.canvas.createBufferStrategy(2);

    return display;
  }

  private final Canvas canvas;
  private final JFrame frame;

  private Display(Canvas canvas, JFrame frame) {
    this.canvas = canvas;
    this.frame  = frame;
  }

  public void draw() { canvas.getBufferStrategy().show(); }

  public void dispose() {
    canvas.getBufferStrategy().dispose();
    frame.dispose();
  }

  public Input createInput() { return Input.init(frame, canvas); }

  public Renderer createRenderer() {
    return Renderer.init(canvas.getBufferStrategy());
  }
}
