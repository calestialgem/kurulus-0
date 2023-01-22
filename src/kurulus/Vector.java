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

  public Vector copy() { return new Vector().set(this); }

  public Vector add(Vector a) {
    this.x += a.x;
    this.y += a.y;
    return this;
  }

  public Vector sub(Vector a) {
    this.x -= a.x;
    this.y -= a.y;
    return this;
  }

  public Vector mul(float s) {
    this.x *= s;
    this.y *= s;
    return this;
  }

  public Vector div(float s) {
    this.x /= s;
    this.y /= s;
    return this;
  }

  public Vector ceil() {
    x = (float) Math.ceil(x);
    y = (float) Math.ceil(y);
    return this;
  }

  public Vector floor() {
    x = (float) Math.floor(x);
    y = (float) Math.floor(y);
    return this;
  }
}
