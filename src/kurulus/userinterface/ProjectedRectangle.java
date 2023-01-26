package kurulus.userinterface;

public record ProjectedRectangle(Rectangle world, Rectangle screen) {
  public ProjectedRectangle() { this(new Rectangle(), new Rectangle()); }
}
