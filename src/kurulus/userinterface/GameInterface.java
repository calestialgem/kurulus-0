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
  private final Vector worldTopLeft;
  private final Vector worldBottomRight;
  private final Vector screenTopLeft;
  private final Vector screenBottomRight;
  private final Vector limitedWorldTopLeft;
  private final Vector limitedWorldBottomRight;
  private final Vector limitedScreenTopLeft;
  private final Vector limitedScreenBottomRight;

  private final Vector cursorOld;
  private final Vector cursorNew;
  private final Vector cursorMovement;

  private final Key panningKey;

  private int zoom;
  private int scale;

  public GameInterface() {
    world       = new World();
    worldOrigin = new Vector();
    worldLimit  = new Vector().set(world.getWidth(), world.getHeight());

    worldTopLeft             = new Vector();
    worldBottomRight         = new Vector();
    screenTopLeft            = new Vector();
    screenBottomRight        = new Vector();
    limitedWorldTopLeft      = new Vector();
    limitedWorldBottomRight  = new Vector();
    limitedScreenTopLeft     = new Vector();
    limitedScreenBottomRight = new Vector();

    cursorOld      = new Vector();
    cursorNew      = new Vector();
    cursorMovement = new Vector();

    final var input = Main.getKurulus().getInput();
    panningKey = input.getMouseKey(MouseEvent.BUTTON2);

    zoom = Kurulus.INITIAL_ZOOM;
    calculateScale();
  }

  @Override public void update() {
    {
      calculateCursorCoordinate(cursorOld);
      zoom += Main.getKurulus().getInput().getWheelRotation();
      calculateScale();
      calculateCursorCoordinate(cursorNew);
      worldTopLeft.add(cursorOld).sub(cursorNew);
    }

    if (panningKey.isDown()) {
      cursorMovement.set(Main.getKurulus().getInput().getCursorMovement());
      worldTopLeft.sub(cursorMovement.div(scale));
    }

    translateToScreenSpace(screenTopLeft.set(worldTopLeft));
    screenBottomRight.set(screenTopLeft).add(Kurulus.WINDOW_SIZE);
    translateToWorldSpace(worldBottomRight.set(screenBottomRight));

    limitedWorldTopLeft.set(worldTopLeft).floor().max(worldOrigin);
    limitedWorldBottomRight.set(worldBottomRight).ceil().min(worldLimit);
    translateToScreenSpace(limitedScreenTopLeft.set(limitedWorldTopLeft));
    translateToScreenSpace(
      limitedScreenBottomRight.set(limitedWorldBottomRight));
  }

  @Override public void render() {
    final var renderer = Main.getKurulus().getRenderer();

    for (var x = limitedWorldTopLeft.x; x < limitedWorldBottomRight.x; x++) {
      for (var y = limitedWorldTopLeft.y; y < limitedWorldBottomRight.y; y++) {
        final var area       =
          world.getArea((int) (x + 0.5f), (int) (y + 0.5f));
        final var coordinate = new Vector().set(x, y);
        translateToScreenSpace(coordinate);
        renderer.fillSquare(coordinate.x, coordinate.y, scale,
          area.terrain.color);
      }
    }

    for (var x = limitedWorldTopLeft.x; x <= limitedWorldBottomRight.x; x++) {
      final var screenX = (x - worldTopLeft.x) * scale;
      renderer.drawLine(screenX, limitedScreenTopLeft.y, screenX,
        limitedScreenBottomRight.y, Kurulus.MAP_GRID_STROKE,
        Kurulus.MAP_GRID_COLOR);
    }

    for (var y = limitedWorldTopLeft.y; y <= limitedWorldBottomRight.y; y++) {
      final var screenY = (y - worldTopLeft.y) * scale;
      renderer.drawLine(limitedScreenTopLeft.x, screenY,
        limitedScreenBottomRight.x, screenY, Kurulus.MAP_GRID_STROKE,
        Kurulus.MAP_GRID_COLOR);
    }
  }

  private Vector calculateCursorCoordinate(Vector vector) {
    return translateToWorldSpace(
      vector.set(Main.getKurulus().getInput().getCursorPosition()));
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
