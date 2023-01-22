package kurulus.userinterface;

import java.awt.event.MouseEvent;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

import kurulus.Kurulus;
import kurulus.Main;
import kurulus.Vector;
import kurulus.display.input.Key;
import kurulus.world.World;

public final class GameInterface implements UserInterface {
  private final World  world;
  private final Vector camera;
  private final Vector cursorOld;
  private final Vector cursorNew;
  private final Vector cursorMovement;
  private final Vector screenTopLeft;
  private final Vector screenBottomRight;

  private final BufferedImage areaImage;

  private final Key panningKey;

  private int   zoom;
  private float scale;

  public GameInterface() throws IOException {
    world             = new World();
    camera            = new Vector();
    cursorOld         = new Vector();
    cursorNew         = new Vector();
    cursorMovement    = new Vector();
    screenTopLeft     = new Vector();
    screenBottomRight = new Vector();

    areaImage = ImageIO.read(GameInterface.class.getResource("tile.png"));

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
      camera.add(cursorOld).sub(cursorNew);
    }

    if (panningKey.isDown()) {
      cursorMovement.set(Main.getKurulus().getInput().getCursorMovement());
      camera.sub(cursorMovement.div(scale));
    }
  }

  @Override public void render() {
    translateToWorldSpace(screenTopLeft.set(0, 0)).floor();
    translateToWorldSpace(
      screenBottomRight.set(Kurulus.WINDOW_WIDTH, Kurulus.WINDOW_HEIGHT))
      .floor();

    final var minX = (int) Math.max(0, screenTopLeft.x);
    final var maxX = (int) Math.min(world.getWidth() - 1, screenBottomRight.x);
    final var minY = (int) Math.max(0, screenTopLeft.y);
    final var maxY = (int) Math.min(world.getHeight() - 1, screenBottomRight.y);

    for (var x = minX; x <= maxX; x++) {
      for (var y = minY; y <= maxY; y++) {
        final var coordinate = new Vector().set(x, y);
        translateToScreenSpace(coordinate);
        Main.getKurulus().getRenderer().drawImage(coordinate.x, coordinate.y,
          scale, areaImage);
      }
    }
  }

  private Vector calculateCursorCoordinate(Vector vector) {
    return translateToWorldSpace(
      vector.set(Main.getKurulus().getInput().getCursorPosition()));
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
    scale = (float) Math.pow(Kurulus.SCALE_BASE, zoom);
  }
}
