package kurulus;

public record Vector(float x, float y) {
  public Vector() { this(0, 0); }

  public Vector add(Vector other) {
    return new Vector(x + other.x, y + other.y);
  }
  public Vector sub(Vector other) {
    return new Vector(x - other.x, y - other.y);
  }

  public Vector mul(float scalar) { return new Vector(x * scalar, y * scalar); }
  public Vector div(float scalar) { return new Vector(x / scalar, y / scalar); }

  public Vector ceil() {
    return new Vector((float) Math.ceil(x), (float) Math.ceil(y));
  }
  public Vector floor() {
    return new Vector((float) Math.floor(x), (float) Math.floor(y));
  }

  public Vector max(Vector other) {
    return new Vector(Math.max(x, other.x), Math.max(y, other.y));
  }
  public Vector min(Vector other) {
    return new Vector(Math.min(x, other.x), Math.min(y, other.y));
  }
}
