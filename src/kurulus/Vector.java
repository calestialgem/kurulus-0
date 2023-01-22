package kurulus;

public class Vector {
  public float x;
  public float y;

  public Vector set(float x, float y) {
    this.x = x;
    this.y = y;
    return this;
  }

  public Vector set(Vector a) {
    this.x = a.x;
    this.y = a.y;
    return this;
  }

  public Vector sub(Vector a) {
    this.x -= a.x;
    this.y -= a.y;
    return this;
  }
}
