import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlightDataAPI implements AutoCloseable {
    private final ChromeDriver driver;
    private final DateTimeFormatter urlDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    FlightDataAPI() {
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);
        options.addArguments("--disable-blink-features",
                "--disable-blink-features=AutomationControlled",
                "user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");
        driver = new ChromeDriver(options);
        Map<String, Object> params = new HashMap<>();
        params.put("source", """
    Object.defineProperty(Navigator.prototype, 'webdriver', {
        set: undefined,
        enumerable: true,
        configurable: true,
        get: new Proxy(
            Object.getOwnPropertyDescriptor(Navigator.prototype, 'webdriver').get,
            { apply: (target, thisArg, args) => {
                Reflect.apply(target, thisArg, args);
                return false;
            }}
        )
    });
""");
        driver.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", params);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
    }

    public List<Flight> getRoundTripNonstopEconomyFlights(String fromCity, String toCity, LocalDate fromDate, LocalDate toDate) {
        driver.get(buildUrl(fromCity, toCity, fromDate, toDate, "&stops=0"));

        List<Flight> flights = findFlightsOnPage(driver, fromCity, toCity, fromDate, toDate);

        if (!driver.findElements(By.cssSelector("[data-testid=\"no_direct_flights_banner\"]")).isEmpty())
        {
            return List.of();
        }

        return flights;
    }

    public List<Flight> getRoundTripEconomyFlights(String fromCity, String toCity, LocalDate fromDate, LocalDate toDate) {
        driver.get(buildUrl(fromCity, toCity, fromDate, toDate, ""));
        return findFlightsOnPage(driver, fromCity, toCity, fromDate, toDate);
    }

    private List<Flight> findFlightsOnPage(WebDriver driver, String fromCity, String toCity, LocalDate fromDate, LocalDate toDate) {
        return driver.findElements(By.cssSelector("[data-testid=\"searchresults_card\"]"))
                .stream()
                .map(p -> {
                    WebElement priceElement = p.findElement(By.cssSelector("[class*=FlightCardPrice-module__priceContainer]"));
                    double price = Double.parseDouble(priceElement.getText().replaceAll("[$,]", ""));
                    WebElement fromStopsElement = p.findElement(By.cssSelector("[data-testid=flight_card_segment_stops_0]"));
                    WebElement toStopsElement = p.findElement(By.cssSelector("[data-testid=flight_card_segment_stops_1]"));
                    int fromStops = fromStopsElement.getText().equals("Direct") ? 0 : Integer.parseInt(fromStopsElement.getText().split(" ")[0]);
                    int toStops = toStopsElement.getText().equals("Direct") ? 0 : Integer.parseInt(toStopsElement.getText().split(" ")[0]);
                    return new Flight(fromCity, toCity, fromDate, toDate, price, fromStops == 0 && toStops == 0);
                })
                .toList();
    }

    String buildUrl(String fromCity, String toCity, LocalDate fromDate, LocalDate toDate, String filters) {
        return "https://flights.booking.com/flights/"+fromCity+".CITY-"+toCity+".CITY/?type=ROUNDTRIP&adults=1&cabinClass=ECONOMY&children=&from="+fromCity+".CITY&to="+toCity+".CITY&depart="+fromDate.format(urlDateFormatter)+"&return="+toDate.format(urlDateFormatter)+"&sort=CHEAPEST&travelPurpose=leisure" + filters;
    }

    @Override
    public void close() {
        driver.quit();
    }
}
