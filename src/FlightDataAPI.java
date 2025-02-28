import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class FlightDataAPI implements AutoCloseable {
    private final WebDriver driver = new ChromeDriver();
    private final DateTimeFormatter urlDateFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");

    FlightDataAPI() {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    public List<Flight> getRoundTripNonstopEconomyFlights(String fromCity, String toCity, LocalDate fromDate, LocalDate toDate) {
        driver.get(buildUrl(fromCity, toCity, fromDate, toDate));
        List<WebElement> listings = driver.findElements(By.cssSelector("[data-test-id=\"offer-listing\"]"));

        return listings.stream()
                .map(l -> l.findElement(By.cssSelector("span.is-visually-hidden.uitk-price-a11y")).getText())
                .mapToInt(p -> Integer.parseInt(p.replaceAll("[$,]", "")))
                .mapToObj(p -> new Flight(fromCity, toCity, fromDate, toDate, p))
                .collect(Collectors.toList());
    }

    String buildUrl(String fromCity, String toCity, LocalDate fromDate, LocalDate toDate) {
        return "https://www.expedia.com/Flights-Search?filters=[{\"numOfStopFilterValue\":{\"stopInfo\":{\"numberOfStops\":0,\"stopFilterOperation\":\"EQUAL\"}}}]&leg1=from:"+fromCity+",to:"+toCity+",departure:"+fromDate.format(urlDateFormatter)+"TANYT,fromType:U,toType:U&leg2=from:"+toCity+",to:"+fromCity+",departure:"+toDate.format(urlDateFormatter)+"TANYT,fromType:U,toType:U&mode=search&options=carrier:,cabinclass:,maxhops:1,nopenalty:N&passengers=adults:1,children:0,infantinlap:N&trip=roundtrip";
    }

    @Override
    public void close() {
        driver.quit();
    }
}
