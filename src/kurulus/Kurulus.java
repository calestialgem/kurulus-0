package kurulus;

import java.awt.event.KeyEvent;

import kurulus.display.Display;
import kurulus.display.Renderer;
import kurulus.display.input.Input;

public final class Kurulus {
  public static final int    MAJOR_VERSION = 0;
  public static final int    MINOR_VERSION = 1;
  public static final int    PATCH_VERSION = 0;
  public static final String VERSION       =
    "%d.%d.%d".formatted(MAJOR_VERSION, MINOR_VERSION, PATCH_VERSION);
  public static final int    WINDOW_WIDTH  = 1280;
  public static final int    WINDOW_HEIGHT = 720;
  public static final double TICK_RATE     = 100.0;

  private final Display display;

  private Input    input;
  private Renderer renderer;
  private boolean  running;
  private double   realTickRate;
  private double   realFrameRate;
  private double   unprocessedTicks;
  private long     currentTick;

  Kurulus() { display = new Display(); }

  public void run() {
    try {
      input            = display.createInput();
      renderer         = display.createRenderer();
      running          = true;
      realTickRate     = 0.0;
      realFrameRate    = 0.0;
      unprocessedTicks = 0.0;
      currentTick      = -1l;

      final var escape = input.getKeyboardKey(KeyEvent.VK_ESCAPE);
      // TODO: Initialize.

      var previousTime = getTime();
      var rateTimer    = 0.0;
      var ticks        = 0;
      var frames       = 0;

      while (running) {
        final var time        = getTime();
        final var elapsedTime = time - previousTime;
        unprocessedTicks += elapsedTime * TICK_RATE;
        previousTime      = time;
        rateTimer        += elapsedTime;

        while (unprocessedTicks >= 1.0) {
          currentTick++;
          input.update();
          if (input.isWindowClosing() || escape.isPressed()) { stop(); }
          // TODO: Update.
          unprocessedTicks--;
          ticks++;
        }

        if (input.isWindowActive() || frames < 1) {
          renderer = display.createRenderer();
          renderer.clear();
          renderer.writeDebugText(5.0f, 5.0f, "tick %d".formatted(currentTick),
            "ups %.0f".formatted(realTickRate),
            "fps %.0f".formatted(realFrameRate),
            "cursor: (%.0f, %.0f)".formatted(input.getCursorPosition().x,
              input.getCursorPosition().y));
          // TODO: Render.
          display.draw();
          frames++;
        }

        if (rateTimer >= 1.0) {
          realTickRate  = ticks / rateTimer;
          realFrameRate = frames / rateTimer;
          rateTimer     = 0.0;
          ticks         = 0;
          frames        = 0;
        }
      }
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }

    // TODO: Dispose.
    display.dispose();
  }

  public void stop() { running = false; }

  private double getTime() { return System.nanoTime() * 1e-9; }
}
