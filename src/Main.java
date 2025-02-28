import java.time.LocalDate;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        FlightDataAPI api = new FlightDataAPI();

        List<Flight> flights = api.getRoundTripNonstopEconomyFlights(
                "Atlanta", "Cancun",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(8)
        );

        flights.forEach(System.out::println);
    }
}