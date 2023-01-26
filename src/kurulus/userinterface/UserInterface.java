package kurulus.userinterface;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Optional;

import kurulus.Kurulus;
import kurulus.Main;
import kurulus.Vector;
import kurulus.display.Renderer;
import kurulus.display.input.Key;
import kurulus.game.Date;
import kurulus.game.Game;
import kurulus.game.world.Area;
import kurulus.game.world.World;

public final class UserInterface {
  private static final int[] DAY_LENGTHS = { Kurulus.convertSecondsToTicks(10),
    Kurulus.convertSecondsToTicks(1), Kurulus.convertSecondsToTicks(0.1), 1 };

  private final Key panningKey;
  private final Key selectingKey;
  private final Key pausingKey;
  private final Key speedingUpKey;
  private final Key speedingDownKey;

  private Game game;

  private ProjectedRectangle visibleRectangle;
  private ProjectedRectangle boundedRectangle;

  private Optional<Area> hoveredArea;
  private Optional<Area> selectedArea;

  private Vector camera;
  private int    zoom;
  private int    scale;

  private int     speed;
  private int     dayCounter;
  private boolean paused;

  public UserInterface(World world) {
    final var input = Main.getKurulus().getInput();
    panningKey      = input.getMouseKey(MouseEvent.BUTTON2);
    selectingKey    = input.getMouseKey(MouseEvent.BUTTON1);
    pausingKey      = input.getKeyboardKey(KeyEvent.VK_SPACE);
    speedingUpKey   = input.getKeyboardKey(KeyEvent.VK_ADD);
    speedingDownKey = input.getKeyboardKey(KeyEvent.VK_SUBTRACT);

    game = new Game(world, new Date(1, 1, 2200), List.of());

    visibleRectangle = new ProjectedRectangle();
    boundedRectangle = new ProjectedRectangle();

    hoveredArea  = Optional.empty();
    selectedArea = Optional.empty();

    camera = new Vector();
    zoom   = Kurulus.INITIAL_ZOOM;
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
      camera = camera.sub(cursorNew.sub(cursorOld));
    }

    if (panningKey.isDown()) {
      final var cursorMovement =
        Main.getKurulus().getInput().getCursorMovement().div(scale);
      camera = camera.sub(cursorMovement);
    }

    final var screenTopLeft   = translateToScreenSpace(camera);
    final var screenRectangle =
      new Rectangle(screenTopLeft, screenTopLeft.add(Kurulus.WINDOW_SIZE));
    visibleRectangle = new ProjectedRectangle(
      translateToWorldSpace(screenRectangle), screenRectangle);

    final var worldBounds             =
      new Rectangle(new Vector(), game.world().getSize());
    final var boundedVisibleRectangle =
      visibleRectangle.world().roundOutwards().intersect(worldBounds);
    boundedRectangle = new ProjectedRectangle(boundedVisibleRectangle,
      translateToScreenSpace(boundedVisibleRectangle));

    if (boundedRectangle.screen()
      .findIntersection(Main.getKurulus().getInput().getCursorPosition())) {
      hoveredArea =
        Optional.of(game.world().getArea(calculateCursorCoordinate().floor()));
    } else {
      hoveredArea = Optional.empty();
    }

    if (Main.getKurulus().getInput().isWindowActive() && hoveredArea.isPresent()
      && selectingKey.isPressed()) {
      selectedArea = hoveredArea;
      hoveredArea  = Optional.empty();
    }

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

  public void render() {
    final var renderer = Main.getKurulus().getRenderer();

    for (var x = boundedRectangle.world().topLeft().getX();
      x < boundedRectangle.world().bottomRight().getX(); x++) {
      for (var y = boundedRectangle.world().topLeft().getY();
        y < boundedRectangle.world().bottomRight().getY(); y++) {
        final var worldCoordinate  = new Vector(x, y);
        final var screenCoordinate = translateToScreenSpace(worldCoordinate);
        renderer.fillSquare(screenCoordinate.x(), screenCoordinate.y(), scale,
          game.world().getArea(worldCoordinate).terrain().color());
      }
    }

    for (var x = boundedRectangle.world().topLeft().getX();
      x <= boundedRectangle.world().bottomRight().getX(); x++) {
      final var screenX = (x - camera.x()) * scale;
      renderer.drawLine(screenX, boundedRectangle.screen().topLeft().y(),
        screenX, boundedRectangle.screen().bottomRight().y(),
        Kurulus.MAP_GRID_STROKE, Kurulus.MAP_GRID_COLOR);
    }

    for (var y = boundedRectangle.world().topLeft().getY();
      y <= boundedRectangle.world().bottomRight().getY(); y++) {
      final var screenY = (y - camera.y()) * scale;
      renderer.drawLine(boundedRectangle.screen().topLeft().x(), screenY,
        boundedRectangle.screen().bottomRight().x(), screenY,
        Kurulus.MAP_GRID_STROKE, Kurulus.MAP_GRID_COLOR);
    }

    final var outlineThickness = Math.max(1, Math.round(scale * 0.02f));
    final var outlineSize      = Math.max(1, scale - outlineThickness * 2);
    if (hoveredArea.isPresent()) {
      final var screen = translateToScreenSpace(hoveredArea.get().coordinate());
      renderer.drawSquare(screen.x() + outlineThickness,
        screen.y() + outlineThickness, outlineSize,
        new BasicStroke(outlineThickness * 2),
        Kurulus.HOVERED_AREA_OUTLINE_COLOR);
    }

    if (selectedArea.isPresent()) {
      final var screen =
        translateToScreenSpace(selectedArea.get().coordinate());
      renderer.drawSquare(
        screen.x() + outlineThickness, screen.y() + outlineThickness,
        outlineSize, new BasicStroke(outlineThickness * 2,
          BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1,
          new float[] { outlineSize / 5f }, Main.getKurulus().getCurrentTick()
            / (float) Kurulus.TICK_RATE * outlineSize),
        Kurulus.SELECTED_AREA_OUTLINE_COLOR);
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

  private Rectangle translateToWorldSpace(Rectangle screenCoordinate) {
    return new Rectangle(translateToWorldSpace(screenCoordinate.topLeft()),
      translateToWorldSpace(screenCoordinate.bottomRight()));
  }

  private Rectangle translateToScreenSpace(Rectangle worldCoordinate) {
    return new Rectangle(translateToScreenSpace(worldCoordinate.topLeft()),
      translateToScreenSpace(worldCoordinate.bottomRight()));
  }

  private Vector translateToWorldSpace(Vector screenCoordinate) {
    return screenCoordinate.div(scale).add(camera);
  }

  private Vector translateToScreenSpace(Vector worldCoordinate) {
    return worldCoordinate.sub(camera).mul(scale);
  }

  private void calculateScale() {
    if (zoom < Kurulus.MINIMUM_ZOOM) { zoom = Kurulus.MINIMUM_ZOOM; }
    if (zoom > Kurulus.MAXIMUM_ZOOM) { zoom = Kurulus.MAXIMUM_ZOOM; }
    scale = (int) (Math.pow(Kurulus.SCALE_BASE, zoom) + 0.5);
  }

  private void resetDayCounter() { dayCounter = DAY_LENGTHS[speed]; }
}
