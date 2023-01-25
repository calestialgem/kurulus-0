package kurulus;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.util.Random;

import kurulus.display.Display;
import kurulus.display.Renderer;
import kurulus.display.input.Input;
import kurulus.game.world.Generator;
import kurulus.game.world.Terrain;
import kurulus.userinterface.GameInterface;
import kurulus.userinterface.UserInterface;

public final class Kurulus {
  public static final int    MAJOR_VERSION = 0;
  public static final int    MINOR_VERSION = 1;
  public static final int    PATCH_VERSION = 0;
  public static final String VERSION       =
    "%d.%d.%d".formatted(MAJOR_VERSION, MINOR_VERSION, PATCH_VERSION);

  public static final int    WINDOW_WIDTH  = 1280;
  public static final int    WINDOW_HEIGHT = 720;
  public static final Vector WINDOW_SIZE   =
    new Vector(WINDOW_WIDTH, WINDOW_HEIGHT);

  public static final double TICK_RATE = 100;

  public static final Vector   WORLD_SIZE                  =
    new Vector(100, 100);
  public static final double[] TERRAIN_ALTITUDE_BOUNDARIES =
    new double[] { 10, 3, 0.15, 0, -0.15 };
  public static final double   NUCLEI_FRACTION             = 0.05;
  public static final double   MIN_NUCLEUS_ALTITUDE        = 8;
  public static final double   MAX_NUCLEUS_ALTITUDE        = 30;
  public static final double   ALTITUDE_DROP_BALANCE       = 5;
  public static final double   ALTITUDE_DROP_MAGNITUDE     = 1;

  public static final double SCALE_BASE   = 1.2;
  public static final int    MINIMUM_ZOOM = 10;
  public static final int    INITIAL_ZOOM = 15;
  public static final int    MAXIMUM_ZOOM = 25;

  public static final Font  DEBUG_FONT       = new Font("Hack", Font.PLAIN, 10);
  public static final Color DEBUG_FOREGROUND = new Color(1f, 1f, 1f);
  public static final Color DEBUG_BACKGROUND = new Color(0, 0, 0, 0.5f);

  public static final Color  MAP_GRID_COLOR  = new Color(1f, 1f, 1f);
  public static final Stroke MAP_GRID_STROKE = new BasicStroke(1f);

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

      final var generator = new Generator(WORLD_SIZE,
        new Terrain[] { new Terrain(Color.RED.darker()),
          new Terrain(Color.YELLOW.darker()), new Terrain(Color.GREEN.darker()),
          new Terrain(Color.YELLOW.brighter()),
          new Terrain(Color.BLUE.brighter()), new Terrain(Color.BLUE),
          new Terrain(Color.BLUE.darker()) },
        TERRAIN_ALTITUDE_BOUNDARIES, NUCLEI_FRACTION, MIN_NUCLEUS_ALTITUDE,
        MAX_NUCLEUS_ALTITUDE, ALTITUDE_DROP_BALANCE, ALTITUDE_DROP_MAGNITUDE);

      userInterface = new GameInterface(generator.generate(new Random()));

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
            "cursor: (%.0f, %.0f)".formatted(input.getCursorPosition().x(),
              input.getCursorPosition().y()));
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
