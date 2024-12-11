import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.AppiumBy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

public class CalcAdditionTest {
    private AndroidDriver driver;

    @BeforeEach
    public void setUp() throws MalformedURLException, URISyntaxException {
        UiAutomator2Options options = new UiAutomator2Options();
        options.setPlatformName("Android");
        options.setUdid("emulator-5554");
        options.setAppPackage("com.google.android.calculator");
        options.setAppActivity("com.android.calculator2.Calculator");
        options.setAutomationName("UiAutomator2");

        driver = new AndroidDriver(new URI("http://127.0.0.1:4723/").toURL(), options);
    }

    @Test
    public void testAddition() {
        int firstNum = 3;
        int secondNum = 5;
        String plusButton = "plus";
        String equalsButton = "equals";
        String additionResultField = "com.google.android.calculator:id/result_final";
        int expectedSum = 8;

        pressNumber(firstNum);
        driver.findElement(AppiumBy.accessibilityId(plusButton)).click();
        pressNumber(secondNum);
        driver.findElement(AppiumBy.accessibilityId(equalsButton)).click();

        WebElement resultElement = driver.findElement(AppiumBy.id(additionResultField));
        String resultText = resultElement.getText();
        int actualResult = Integer.parseInt(resultText);

        System.out.println("Result: " + actualResult);
        Assertions.assertEquals(expectedSum, actualResult, "Addition result is incorrect!");
    }

    // For loop here in case if there are multiple digits to enter
    private void pressNumber(int number) {
        String numStr = String.valueOf(number);
        for (char digit : numStr.toCharArray()) {
            driver.findElement(AppiumBy.accessibilityId(String.valueOf(digit))).click();
        }
    }


    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
