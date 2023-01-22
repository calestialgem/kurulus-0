package kurulus;

public class Main {
  private static Kurulus kurulus;

  public static void main(String[] arguments) {
    System.out.printf("Kuruluş %s%n", Kurulus.VERSION);
    kurulus = new Kurulus();
    kurulus.run();
  }

  public static Kurulus getKurulus() { return kurulus; }
}
