package kurulus.game;

public record Date(int day, int month, int year) {
  public Date findNextDay() {
    if (!isEndOfMonth()) { return new Date(day + 1, month, year); }
    if (!isEndOfYear()) { return new Date(1, month + 1, year); }
    return new Date(1, 1, year + 1);
  }
  public boolean isEndOfMonth() { return day == getMonthLength(); }
  public boolean isEndOfYear() { return month == 12; }

  private int getMonthLength() {
    return switch (month) {
    case 1 -> 31;
    case 2 -> findYearStatus() ? 29 : 28;
    case 3 -> 31;
    case 4 -> 30;
    case 5 -> 31;
    case 6 -> 30;
    case 7 -> 31;
    case 8 -> 31;
    case 9 -> 30;
    case 10 -> 31;
    case 11 -> 30;
    case 12 -> 31;
    default -> throw new RuntimeException();
    };
  }

  private boolean findYearStatus() {
    return year % 4 == 0 && year % 100 != 0 || year % 400 == 0;
  }
}
