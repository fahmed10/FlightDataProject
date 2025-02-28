import java.time.LocalDate;

public class Flight {
    private final int price;
    private final String fromCity, toCity;
    private final LocalDate fromDate, toDate;

    public Flight(String fromCity, String toCity, LocalDate fromDate, LocalDate toDate, int price) {
        this.fromCity = fromCity;
        this.toCity = toCity;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.price = price;
    }

    public int getPrice() {
        return price;
    }

    public String getFromCity() {
        return fromCity;
    }

    public String getToCity() {
        return toCity;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    @Override
    public String toString() {
        return fromCity + " <-> " + toCity + " from " + fromDate + " to " + toDate + " ($" + price + ")";
    }
}
