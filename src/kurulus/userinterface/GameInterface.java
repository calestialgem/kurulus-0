package kurulus.userinterface;

import java.awt.event.MouseEvent;

import kurulus.Kurulus;
import kurulus.Main;
import kurulus.Vector;
import kurulus.display.input.Key;
import kurulus.world.World;

public final class GameInterface implements UserInterface {
  private final World world;
  private final Key   panningKey;

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

  public GameInterface(World world) {
    this.world = world;
    panningKey = Main.getKurulus().getInput().getMouseKey(MouseEvent.BUTTON2);

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
  }

  @Override public void update() {
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
    limitedWorldBottomRight  = worldBottomRight.ceil().min(world.getSize());
    limitedScreenTopLeft     = translateToScreenSpace(limitedWorldTopLeft);
    limitedScreenBottomRight = translateToScreenSpace(limitedWorldBottomRight);
  }

  @Override public void render() {
    final var renderer = Main.getKurulus().getRenderer();

    for (var x = limitedWorldTopLeft.getX(); x < limitedWorldBottomRight.getX();
      x++) {
      for (var y = limitedWorldTopLeft.getY();
        y < limitedWorldBottomRight.getY(); y++) {
        final var worldCoordinate  = new Vector(x, y);
        final var screenCoordinate = translateToScreenSpace(worldCoordinate);
        renderer.fillSquare(screenCoordinate.x(), screenCoordinate.y(), scale,
          world.getArea(worldCoordinate).terrain().color());
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
