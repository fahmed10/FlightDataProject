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
import java.util.stream.Collectors;

public class FlightDataAPI implements AutoCloseable {
    private final ChromeDriver driver;
    private final DateTimeFormatter urlDateFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");

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
                // emulate getter call validation
                Reflect.apply(target, thisArg, args);
                return undefined;
            }}
        )
    });
""");
        driver.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", params);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));
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
