import org.junit.*;

import java.time.LocalDate;
import java.time.Month;

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
                LocalDate.of(2026, Month.JANUARY, 9)
        );

        Assert.assertEquals("https://www.expedia.com/Flights-Search?filters=[{\"numOfStopFilterValue\":{\"stopInfo\":{\"numberOfStops\":0,\"stopFilterOperation\":\"EQUAL\"}}}]&leg1=from:Atlanta,to:Cancun,departure:12/2/2025TANYT,fromType:U,toType:U&leg2=from:Cancun,to:Atlanta,departure:1/9/2026TANYT,fromType:U,toType:U&mode=search&options=carrier:,cabinclass:,maxhops:1,nopenalty:N&passengers=adults:1,children:0,infantinlap:N&trip=roundtrip", url);
    }
}
