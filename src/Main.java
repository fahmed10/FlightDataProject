import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class Main {
    private static final SQLiteDatabase database = new SQLiteDatabase("flight_data.sqlite", Main::onSQLiteError);
    private static final LocalDate startDate = LocalDate.of(2025, 5, 1); // May 1st
    private static final LocalDate endDate = LocalDate.of(2025, 8, 15); // Aug. 15th
    private static final String[] cities = { "Cancun", "Las Vegas", "Denver", "Rome", "Milan", "Paris", "Madrid", "Amsterdam", "Singapore" };

    public static void main(String[] args) {
        FlightDataAPI api = new FlightDataAPI();

        for (String city : cities) {
            LocalDate currentStartDate = startDate;

            // Go through range of dates for this city
            while (currentStartDate.plusWeeks(1).isBefore(endDate.plusDays(1))) {
                List<Flight> flights = api.getRoundTripNonstopEconomyFlights(
                        "Atlanta", city,
                        currentStartDate,
                        currentStartDate.plusWeeks(1)
                );

                // TODO: Save to database instead of printing for each flight
                flights.forEach(System.out::println);
                System.out.println();
                currentStartDate = currentStartDate.plusDays(1);
            }
        }

        api.close();

        // TODO: Query database to get cheapest flight for each city, print it
    }

    private static void onSQLiteError(SQLException exception) {
        System.out.println("[SQLite Error] " + exception.getMessage());
    }
}