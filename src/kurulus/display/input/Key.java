package kurulus.display.input;

public final class Key {
  private boolean previous;
  private boolean down;
  private boolean pressed;
  private boolean released;

  public boolean isDown() { return down; }
  public boolean isPressed() { return pressed; }
  public boolean isReleased() { return released; }

  void update() {
    pressed  = !previous && down;
    released = previous && !down;
    previous = down;
  }

  void setDown(boolean down) { this.down = down; }
}
