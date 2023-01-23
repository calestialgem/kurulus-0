package kurulus.userinterface;

import java.awt.event.MouseEvent;

import kurulus.Kurulus;
import kurulus.Main;
import kurulus.Vector;
import kurulus.display.input.Key;
import kurulus.world.World;

public final class GameInterface implements UserInterface {
  private final World  world;
  private final Vector worldOrigin;
  private final Vector worldLimit;
  private final Key    panningKey;

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

  public GameInterface() {
    world       = new World();
    worldOrigin = new Vector();
    worldLimit  = new Vector(world.getWidth(), world.getHeight());
    panningKey  = Main.getKurulus().getInput().getMouseKey(MouseEvent.BUTTON2);

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

    limitedWorldTopLeft      = worldTopLeft.floor().max(worldOrigin);
    limitedWorldBottomRight  = worldBottomRight.ceil().min(worldLimit);
    limitedScreenTopLeft     = translateToScreenSpace(limitedWorldTopLeft);
    limitedScreenBottomRight = translateToScreenSpace(limitedWorldBottomRight);
  }

  @Override public void render() {
    final var renderer = Main.getKurulus().getRenderer();

    for (var x = limitedWorldTopLeft.x(); x < limitedWorldBottomRight.x();
      x++) {
      for (var y = limitedWorldTopLeft.y(); y < limitedWorldBottomRight.y();
        y++) {
        final var area       =
          world.getArea((int) (x + 0.5f), (int) (y + 0.5f));
        final var coordinate = translateToScreenSpace(new Vector(x, y));
        renderer.fillSquare(coordinate.x(), coordinate.y(), scale,
          area.terrain.color);
      }
    }

    for (var x = limitedWorldTopLeft.x(); x <= limitedWorldBottomRight.x();
      x++) {
      final var screenX = (x - worldTopLeft.x()) * scale;
      renderer.drawLine(screenX, limitedScreenTopLeft.y(), screenX,
        limitedScreenBottomRight.y(), Kurulus.MAP_GRID_STROKE,
        Kurulus.MAP_GRID_COLOR);
    }

    for (var y = limitedWorldTopLeft.y(); y <= limitedWorldBottomRight.y();
      y++) {
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
