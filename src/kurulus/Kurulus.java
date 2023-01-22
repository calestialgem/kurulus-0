package kurulus;

import java.awt.event.KeyEvent;

import kurulus.display.Display;
import kurulus.display.Renderer;
import kurulus.display.input.Input;
import kurulus.userinterface.GameInterface;
import kurulus.userinterface.UserInterface;
import java.awt.Font;
import java.awt.Color;

public final class Kurulus {
  public static final int    MAJOR_VERSION = 0;
  public static final int    MINOR_VERSION = 1;
  public static final int    PATCH_VERSION = 0;
  public static final String VERSION       =
    "%d.%d.%d".formatted(MAJOR_VERSION, MINOR_VERSION, PATCH_VERSION);

  public static final int WINDOW_WIDTH  = 1280;
  public static final int WINDOW_HEIGHT = 720;

  public static final double TICK_RATE = 100;

  public static final int WORLD_SIZE = 100;

  public static final float SCALE_BASE   = 1.2f;
  public static final int   MINIMUM_ZOOM = 10;
  public static final int   INITIAL_ZOOM = 15;
  public static final int   MAXIMUM_ZOOM = 25;

  public static final Font  DEBUG_FONT       = new Font("Hack", Font.PLAIN, 10);
  public static final Color DEBUG_FOREGROUND = new Color(1f, 1f, 1f);
  public static final Color DEBUG_BACKGROUND = new Color(0, 0, 0, 0.5f);

  private final Display display;

  private Input         input;
  private Renderer      renderer;
  private boolean       running;
  private double        realTickRate;
  private double        realFrameRate;
  private double        unprocessedTicks;
  private long          currentTick;
  private UserInterface userInterface;

  Kurulus() { display = new Display(); }

  public void run() {
    try {
      input            = display.createInput();
      renderer         = display.createRenderer();
      running          = true;
      realTickRate     = 0;
      realFrameRate    = 0;
      unprocessedTicks = 0;
      currentTick      = -1;
      userInterface    = new GameInterface();

      final var escape = input.getKeyboardKey(KeyEvent.VK_ESCAPE);

      var previousTime = getTime();
      var rateTimer    = 0d;
      var ticks        = 0;
      var frames       = 0;

      while (running) {
        final var time        = getTime();
        final var elapsedTime = time - previousTime;
        unprocessedTicks += elapsedTime * TICK_RATE;
        previousTime      = time;
        rateTimer        += elapsedTime;

        while (unprocessedTicks >= 1) {
          currentTick++;
          input.update();
          if (input.isWindowClosing() || escape.isPressed()) { stop(); }
          userInterface.update();
          unprocessedTicks--;
          ticks++;
        }

        if (input.isWindowActive() || frames < 1) {
          renderer = display.createRenderer();
          renderer.clear();
          userInterface.render();
          renderer.write(5, 5, DEBUG_FOREGROUND, DEBUG_BACKGROUND, DEBUG_FONT,
            "tick %d".formatted(currentTick),
            "ups %.0f".formatted(realTickRate),
            "fps %.0f".formatted(realFrameRate),
            "cursor: (%.0f, %.0f)".formatted(input.getCursorPosition().x,
              input.getCursorPosition().y));
          display.draw();
          frames++;
        }

        if (rateTimer >= 1) {
          realTickRate  = ticks / rateTimer;
          realFrameRate = frames / rateTimer;
          rateTimer     = 0;
          ticks         = 0;
          frames        = 0;
        }
      }
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }

    display.dispose();
  }

  public void stop() { running = false; }
  public Input getInput() { return input; }
  public Renderer getRenderer() { return renderer; }
  public double getUnprocessedTicks() { return unprocessedTicks; }
  public long getCurrentTick() { return currentTick; }

  private double getTime() { return System.nanoTime() * 1e-9; }
}
