package kurulus;

public record Vector(float x, float y) {
  public Vector() { this(0, 0); }

  public Vector add(Vector other) {
    return new Vector(x + other.x, y + other.y);
  }
  public Vector sub(Vector other) {
    return new Vector(x - other.x, y - other.y);
  }
  public Vector mul(Vector other) {
    return new Vector(x * other.x, y * other.y);
  }
  public Vector div(Vector other) {
    return new Vector(x / other.x, y / other.y);
  }

  public Vector addX(float x) { return new Vector(this.x + x, y); }
  public Vector addY(float y) { return new Vector(x, this.y + y); }
  public Vector subX(float x) { return new Vector(this.x - x, y); }
  public Vector subY(float y) { return new Vector(x, this.y - y); }

  public Vector mul(float scalar) { return new Vector(x * scalar, y * scalar); }
  public Vector div(float scalar) { return new Vector(x / scalar, y / scalar); }

  public Vector ceil() {
    return new Vector((float) Math.ceil(x), (float) Math.ceil(y));
  }
  public Vector floor() {
    return new Vector((float) Math.floor(x), (float) Math.floor(y));
  }

  public int getX() { return (int) (x + 0.5f); }
  public int getY() { return (int) (y + 0.5f); }

  public Vector max(Vector other) {
    return new Vector(Math.max(x, other.x), Math.max(y, other.y));
  }
  public Vector min(Vector other) {
    return new Vector(Math.min(x, other.x), Math.min(y, other.y));
  }
}
