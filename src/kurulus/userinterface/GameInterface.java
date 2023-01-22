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

  private final BufferedImage areaImage;

  private final Key panningKey;

  private int   zoom;
  private float scale;

  public GameInterface() throws IOException {
    world  = new World();
    camera = new Vector();

    areaImage = ImageIO.read(GameInterface.class.getResource("tile.png"));

    final var input = Main.getKurulus().getInput();
    panningKey = input.getMouseKey(MouseEvent.BUTTON2);

    zoom = Kurulus.INITIAL_ZOOM;
    calculateScale();
  }

  @Override public void update() {
    {
      final var cursorOld = new Vector();
      cursorOld.set(Main.getKurulus().getInput().getCursorPosition());
      translateToWorldSpace(cursorOld);

      zoom += Main.getKurulus().getInput().getWheelRotation();
      calculateScale();

      final var cursorNew = new Vector();
      cursorNew.set(Main.getKurulus().getInput().getCursorPosition());
      translateToWorldSpace(cursorNew);

      camera.add(cursorOld).sub(cursorNew);
    }
  }

  @Override public void render() {
    final var topLeft     = new Vector();
    final var bottomRight =
      new Vector().set(Kurulus.WINDOW_WIDTH, Kurulus.WINDOW_HEIGHT);

    translateToWorldSpace(topLeft);
    translateToWorldSpace(bottomRight);

    topLeft.floor();
    bottomRight.ceil();

    final var minX = (int) Math.max(0, topLeft.x);
    final var maxX = (int) Math.min(world.getWidth() - 1, bottomRight.x);
    final var minY = (int) Math.max(0, topLeft.y);
    final var maxY = (int) Math.min(world.getHeight() - 1, bottomRight.y);

    for (var x = minX; x <= maxX; x++) {
      for (var y = minY; y <= maxY; y++) {
        final var coordinate = new Vector().set(x, y);
        translateToScreenSpace(coordinate);
        Main.getKurulus().getRenderer().drawImage(coordinate.x, coordinate.y,
          scale, areaImage);
      }
    }
  }

  private void translateToWorldSpace(Vector screenCoordinate) {
    screenCoordinate.div(scale).add(camera);
  }
  private void translateToScreenSpace(Vector worldCoordinate) {
    worldCoordinate.sub(camera).mul(scale);
  }

  private void calculateScale() {
    if (zoom < Kurulus.MINIMUM_ZOOM) { zoom = Kurulus.MINIMUM_ZOOM; }
    if (zoom > Kurulus.MAXIMUM_ZOOM) { zoom = Kurulus.MAXIMUM_ZOOM; }
    scale = (float) Math.pow(Kurulus.SCALE_BASE, zoom);
  }
}
