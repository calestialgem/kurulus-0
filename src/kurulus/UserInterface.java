package kurulus;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Optional;

import kurulus.display.Renderer;
import kurulus.display.input.Key;
import kurulus.game.Game;
import kurulus.game.State;
import kurulus.game.world.Area;

public final class UserInterface {
  private static final int[] DAY_LENGTHS = { Kurulus.convertSecondsToTicks(10),
    Kurulus.convertSecondsToTicks(1), Kurulus.convertSecondsToTicks(0.1), 1 };

  private static final Font USERINTERFACE_FONT =
    new Font("Inter", Font.PLAIN, 20);

  private final Game game;

  private final Key panningKey;
  private final Key selectingKey;
  private final Key pausingKey;
  private final Key speedingUpKey;
  private final Key speedingDownKey;
  private final Key settlingKey;

  private State controlled;

  private Vector worldTopLeft;
  private Vector worldBottomRight;
  private Vector screenTopLeft;
  private Vector screenBottomRight;
  private Vector limitedWorldTopLeft;
  private Vector limitedWorldBottomRight;
  private Vector limitedScreenTopLeft;
  private Vector limitedScreenBottomRight;

  private Optional<Area> hoveredArea;
  private Optional<Area> selectedArea;

  private int zoom;
  private int scale;

  private int     speed;
  private int     dayCounter;
  private boolean paused;

  public UserInterface(Game game) {
    this.game = game;

    final var input = Main.getKurulus().getInput();
    panningKey      = input.getMouseKey(MouseEvent.BUTTON2);
    selectingKey    = input.getMouseKey(MouseEvent.BUTTON1);
    pausingKey      = input.getKeyboardKey(KeyEvent.VK_SPACE);
    speedingUpKey   = input.getKeyboardKey(KeyEvent.VK_ADD);
    speedingDownKey = input.getKeyboardKey(KeyEvent.VK_SUBTRACT);
    settlingKey     = input.getKeyboardKey(KeyEvent.VK_S);

    controlled = game.createState("Republic of Turkey", Color.RED);

    worldTopLeft             = new Vector();
    worldBottomRight         = new Vector();
    screenTopLeft            = new Vector();
    screenBottomRight        = new Vector();
    limitedWorldTopLeft      = new Vector();
    limitedWorldBottomRight  = new Vector();
    limitedScreenTopLeft     = new Vector();
    limitedScreenBottomRight = new Vector();

    hoveredArea  = Optional.empty();
    selectedArea = Optional.empty();

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
      worldBottomRight.ceil().min(game.world.getSize());
    limitedScreenTopLeft     = translateToScreenSpace(limitedWorldTopLeft);
    limitedScreenBottomRight = translateToScreenSpace(limitedWorldBottomRight);

    final var cursorWorld = calculateCursorCoordinate();
    if (cursorWorld.x() > limitedWorldTopLeft.x()
      && cursorWorld.y() > limitedWorldTopLeft.y()
      && cursorWorld.x() < limitedWorldBottomRight.x()
      && cursorWorld.y() < limitedWorldBottomRight.y()) {
      hoveredArea = Optional.of(game.world.getArea(cursorWorld.floor()));
    } else {
      hoveredArea = Optional.empty();
    }

    if (Main.getKurulus().getInput().isWindowActive() && hoveredArea.isPresent()
      && selectingKey.isPressed()) {
      selectedArea = hoveredArea;
      hoveredArea  = Optional.empty();
    }

    if (settlingKey.isPressed() && selectedArea.isPresent()
      && game.settle(selectedArea.get().coordinate(), controlled)) {
      selectedArea = Optional.empty();
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
        game.simulateToday();
      }
    }
  }

  public void render() {
    final var renderer = Main.getKurulus().getRenderer();

    for (var x = limitedWorldTopLeft.getX(); x < limitedWorldBottomRight.getX();
      x++) {
      for (var y = limitedWorldTopLeft.getY();
        y < limitedWorldBottomRight.getY(); y++) {
        final var worldCoordinate  = new Vector(x, y);
        final var screenCoordinate = translateToScreenSpace(worldCoordinate);
        renderer.fillSquare(screenCoordinate.x(), screenCoordinate.y(), scale,
          game.world.getArea(worldCoordinate).terrain().color());
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

    for (final var settlement : game.getSettlements()) {
      final var screenCoordinate =
        translateToScreenSpace(settlement.area().coordinate());
      final var size             = Math.max(3, scale * 0.2f);
      renderer.fillCircle(screenCoordinate.x() + (scale - size) / 2,
        screenCoordinate.y() + (scale - size) / 2, size, Color.BLACK);
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

    {
      var y = Kurulus.WINDOW_HEIGHT * 0.1f;
      renderer.write(5, y, controlled.color(), Color.BLACK, USERINTERFACE_FONT,
        "%s: %d".formatted(controlled.name(),
          game.getSettlements(controlled).size()));
      y += 10;

      for (final var state : game.getStates()) {
        if (state.equals(controlled)) { continue; }
        y += renderer.getHeight(USERINTERFACE_FONT);
        renderer.write(5, y, state.color(), Color.BLACK, USERINTERFACE_FONT,
          "%s: %d".formatted(state.name(), game.getSettlements(state).size()));
      }
    }

    renderer.write(Kurulus.WINDOW_WIDTH - 5, 5, Color.WHITE, Color.BLACK,
      USERINTERFACE_FONT, Renderer.HorizontalAlignment.RIGHT,
      "%02d.%02d.%d".formatted(game.getDate().day(), game.getDate().month(),
        game.getDate().year()),
      "Speed: %d".formatted(speed + 1));

    if (paused) {
      renderer.write(Kurulus.WINDOW_WIDTH / 2, Kurulus.WINDOW_HEIGHT * 0.05f,
        Color.WHITE, Color.BLACK, USERINTERFACE_FONT,
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

  private void resetDayCounter() { dayCounter = DAY_LENGTHS[speed]; }
}
