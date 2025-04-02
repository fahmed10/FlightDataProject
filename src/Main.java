import java.sql.ResultSet;
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

        setFlightTables();


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
                // Save to database
                String insertQuery = "INSERT INTO flights (from_city, to_city, from_date, to_date, price) VALUES (?, ?, ?, ?, ?)";
                for (Flight flight : flights) {
                    database.updatePrepared(insertQuery, flight.getFromCity(),
                            flight.getToCity(), flight.getFromDate().toString(),
                            flight.getToDate().toString(), flight.getPrice());
                }

                // Print (for testing purposes)
                flights.forEach(System.out::println);
                System.out.println();
                currentStartDate = currentStartDate.plusDays(1);
            }
        }

        api.close();

        printCheapestFlights();

    }

    private static void setFlightTables() {
        // Check if the flights table exists
        ResultSet resultSet = null;

        try {
            // Query the database schema to check if the flights table exists
            resultSet = database.query("SELECT name FROM sqlite_master WHERE type='table' AND name='flights'");

            // If the table exists, clear its data
            if (resultSet.next()) {
                database.update("DELETE FROM flights");
                System.out.println("Flight data cleared from the existing table.");
            } else {
                // If table doesn't exist, create table
                database.update("CREATE TABLE IF NOT EXISTS flights (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "from_city TEXT NOT NULL, to_city TEXT NOT NULL, " +
                        "from_date TEXT NOT NULL, to_date TEXT NOT NULL, " +
                        "price REAL NOT NULL)");
                System.out.println("Flights table created successfully.");
            }
        } catch (SQLException e) {
            onSQLiteError(e);
        } finally {
            try {
                if (resultSet != null) resultSet.getStatement().close();
            } catch (SQLException e) {
                onSQLiteError(e);
            }
        }
    }

    private static void printCheapestFlights() {
        // Query database to get the cheapest flight for each city, print it
        String cheapestFlightsQuery = "SELECT to_city, MIN(price) AS cheapest_price, from_city, from_date, to_date FROM flights GROUP BY to_city";

        try (ResultSet resultSet = database.query(cheapestFlightsQuery)) {
            while (resultSet.next()) {
                String toCity = resultSet.getString("to_city");
                double price = resultSet.getDouble("cheapest_price");
                String fromCity = resultSet.getString("from_city");
                String fromDate = resultSet.getString("from_date");
                String toDate = resultSet.getString("to_date");

                System.out.printf("Cheapest flight to %s: %s -> %s from %s to %s ($%.2f)%n",
                        toCity, fromCity, toCity, fromDate, toDate, price);
            }
        } catch (SQLException e) {
            onSQLiteError(e);
        }
    }

    private static void onSQLiteError(SQLException exception) {
        System.out.println("[SQLite Error] " + exception.getMessage());
    }
}