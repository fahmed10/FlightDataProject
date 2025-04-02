import org.junit.*;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

public class FlightDataAPITests {
    private static FlightDataAPI flightDataAPI;

    @BeforeClass
    public static void setUp() {
        flightDataAPI = new FlightDataAPI();
    }

    @AfterClass
    public static void tearDown() {
        flightDataAPI.close();
    }

    @Test
    public void testBuildUrl() {
        String url = flightDataAPI.buildUrl(
                "Atlanta", "Cancun",
                LocalDate.of(2025, Month.DECEMBER, 2),
                LocalDate.of(2026, Month.JANUARY, 9),
                "&test=3"
        );

        Assert.assertEquals("https://flights.booking.com/flights/Atlanta.CITY-Cancun.CITY/?type=ROUNDTRIP&adults=1&cabinClass=ECONOMY&children=&from=Atlanta.CITY&to=Cancun.CITY&depart=2025-12-02&return=2026-01-09&sort=CHEAPEST&travelPurpose=leisure&test=3", url);
    }

    @Test
    public void testGetRoundTripEconomyFlights() {
        String[] cities = { "Rome", "Milan", "Paris", "Madrid", "Amsterdam", "Singapore" };
        for (String city : cities) {
            LocalDate start = LocalDate.now().plusWeeks(3);
            LocalDate end = LocalDate.now().plusWeeks(4);

            List<Flight> flights = new ArrayList<>();
            while (flights.isEmpty()) {
                flights = flightDataAPI.getRoundTripEconomyFlights("Atlanta", city, start, end);
                start = start.plusDays(1);
                end = end.plusDays(1);
            }

            for (Flight flight : flights) {
                Assert.assertEquals("Atlanta", flight.getFromCity());
                Assert.assertEquals(city, flight.getToCity());
                Assert.assertEquals(start, flight.getFromDate());
                Assert.assertEquals(end, flight.getToDate());
                Assert.assertTrue(flight.getPrice() > 0);
            }
        }
    }
}
