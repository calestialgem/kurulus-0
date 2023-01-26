package kurulus;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import kurulus.display.Renderer;
import kurulus.display.input.Key;
import kurulus.game.Date;
import kurulus.game.Game;
import kurulus.game.world.World;

public final class UserInterface {
  private static final int[] DAY_LENGTHS = { Kurulus.convertSecondsToTicks(10),
    Kurulus.convertSecondsToTicks(1), Kurulus.convertSecondsToTicks(0.1), 1 };

  private final Key panningKey;
  private final Key pausingKey;
  private final Key speedingUpKey;
  private final Key speedingDownKey;

  private Game game;

  private Vector worldTopLeft;
  private Vector worldBottomRight;
  private Vector screenTopLeft;
  private Vector screenBottomRight;
  private Vector limitedWorldTopLeft;
  private Vector limitedWorldBottomRight;
  private Vector limitedScreenTopLeft;
  private Vector limitedScreenBottomRight;

  private int zoom;
  private int scale;

  private int     speed;
  private int     dayCounter;
  private boolean paused;

  public UserInterface(World world) {
    final var input = Main.getKurulus().getInput();
    panningKey      = input.getMouseKey(MouseEvent.BUTTON2);
    pausingKey      = input.getKeyboardKey(KeyEvent.VK_SPACE);
    speedingUpKey   = input.getKeyboardKey(KeyEvent.VK_ADD);
    speedingDownKey = input.getKeyboardKey(KeyEvent.VK_SUBTRACT);

    game = new Game(world, new Date(1, 1, 2200));

    worldTopLeft             = new Vector();
    worldBottomRight         = new Vector();
    screenTopLeft            = new Vector();
    screenBottomRight        = new Vector();
    limitedWorldTopLeft      = new Vector();
    limitedWorldBottomRight  = new Vector();
    limitedScreenTopLeft     = new Vector();
    limitedScreenBottomRight = new Vector();

    zoom = Kurulus.INITIAL_ZOOM;
    calculateScale();

    speed      = 0;
    dayCounter = 0;
    paused     = true;
    resetDayCounter();
  }

  public void update() {
    {
      final var cursorOld = calculateCursorCoordinate();
      zoom += Main.getKurulus().getInput().getWheelRotation();
      calculateScale();
      final var cursorNew = calculateCursorCoordinate();
      worldTopLeft = worldTopLeft.sub(cursorNew.sub(cursorOld));
    }

    if (panningKey.isDown()) {
      final var cursorMovement =
        Main.getKurulus().getInput().getCursorMovement().div(scale);
      worldTopLeft = worldTopLeft.sub(cursorMovement);
    }

    screenTopLeft     = translateToScreenSpace(worldTopLeft);
    screenBottomRight = screenTopLeft.add(Kurulus.WINDOW_SIZE);
    worldBottomRight  = translateToWorldSpace(screenBottomRight);

    limitedWorldTopLeft      = worldTopLeft.floor().max(new Vector());
    limitedWorldBottomRight  =
      worldBottomRight.ceil().min(game.world().getSize());
    limitedScreenTopLeft     = translateToScreenSpace(limitedWorldTopLeft);
    limitedScreenBottomRight = translateToScreenSpace(limitedWorldBottomRight);

    if (pausingKey.isPressed()) {
      paused = !paused;
      if (paused) { resetDayCounter(); }
    }

    if (speedingUpKey.isPressed()) {
      speed++;
      if (speed >= DAY_LENGTHS.length) { speed = DAY_LENGTHS.length - 1; }
      resetDayCounter();
    }

    if (speedingDownKey.isPressed()) {
      speed--;
      if (speed < 0) { speed = 0; }
      resetDayCounter();
    }

    if (!paused) {
      dayCounter--;
      if (dayCounter == 0) {
        resetDayCounter();
        game = game.simulateToday();
      }
    }
  }

  private void resetDayCounter() { dayCounter = DAY_LENGTHS[speed]; }

  public void render() {
    final var renderer = Main.getKurulus().getRenderer();

    for (var x = limitedWorldTopLeft.getX(); x < limitedWorldBottomRight.getX();
      x++) {
      for (var y = limitedWorldTopLeft.getY();
        y < limitedWorldBottomRight.getY(); y++) {
        final var worldCoordinate  = new Vector(x, y);
        final var screenCoordinate = translateToScreenSpace(worldCoordinate);
        renderer.fillSquare(screenCoordinate.x(), screenCoordinate.y(), scale,
          game.world().getArea(worldCoordinate).terrain().color());
      }
    }

    for (var x = limitedWorldTopLeft.getX();
      x <= limitedWorldBottomRight.getX(); x++) {
      final var screenX = (x - worldTopLeft.x()) * scale;
      renderer.drawLine(screenX, limitedScreenTopLeft.y(), screenX,
        limitedScreenBottomRight.y(), Kurulus.MAP_GRID_STROKE,
        Kurulus.MAP_GRID_COLOR);
    }

    for (var y = limitedWorldTopLeft.getY();
      y <= limitedWorldBottomRight.getY(); y++) {
      final var screenY = (y - worldTopLeft.y()) * scale;
      renderer.drawLine(limitedScreenTopLeft.x(), screenY,
        limitedScreenBottomRight.x(), screenY, Kurulus.MAP_GRID_STROKE,
        Kurulus.MAP_GRID_COLOR);
    }

    renderer.write(Kurulus.WINDOW_WIDTH - 5, 5, Color.WHITE, Color.BLACK,
      new Font("Inter", Font.PLAIN, 20), Renderer.HorizontalAlignment.RIGHT,
      "%02d.%02d.%d".formatted(game.date().day(), game.date().month(),
        game.date().year()),
      "Speed: %d".formatted(speed + 1));

    if (paused) {
      renderer.write(Kurulus.WINDOW_WIDTH / 2, Kurulus.WINDOW_HEIGHT * 0.05f,
        Color.WHITE, Color.BLACK, new Font("Inter", Font.PLAIN, 32),
        Renderer.HorizontalAlignment.CENTER, "P A U S E D");
    }
  }

  private Vector calculateCursorCoordinate() {
    return translateToWorldSpace(
      Main.getKurulus().getInput().getCursorPosition());
  }

  private Vector translateToWorldSpace(Vector screenCoordinate) {
    return screenCoordinate.div(scale).add(worldTopLeft);
  }

  private Vector translateToScreenSpace(Vector worldCoordinate) {
    return worldCoordinate.sub(worldTopLeft).mul(scale);
  }

  private void calculateScale() {
    if (zoom < Kurulus.MINIMUM_ZOOM) { zoom = Kurulus.MINIMUM_ZOOM; }
    if (zoom > Kurulus.MAXIMUM_ZOOM) { zoom = Kurulus.MAXIMUM_ZOOM; }
    scale = (int) (Math.pow(Kurulus.SCALE_BASE, zoom) + 0.5);
  }
}
