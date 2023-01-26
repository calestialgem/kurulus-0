package kurulus.display;

public final class Key {
  static Key init() { return new Key(false, false, false, false); }

  private boolean previous;
  private boolean down;
  private boolean pressed;
  private boolean released;

  private Key(boolean previous, boolean down, boolean pressed,
    boolean released) {
    this.previous = previous;
    this.down     = down;
    this.pressed  = pressed;
    this.released = released;
  }

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
