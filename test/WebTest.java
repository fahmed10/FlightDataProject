import org.junit.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class WebTest {
    private static WebDriver driver;

    @BeforeClass
    public static void setup() {
        driver = new ChromeDriver();
    }

    @AfterClass
    public static void teardown() {
        driver.quit();
    }

    @Test
    public void test() {
        driver.get("https://www.google.com");
    }
}
