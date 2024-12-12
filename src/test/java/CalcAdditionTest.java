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
    public void setUp() {
        UiAutomator2Options options = new UiAutomator2Options();
        options.setPlatformName("Android");
        options.setUdid("emulator-5554");
        options.setAppPackage("com.google.android.calculator");
        options.setAppActivity("com.android.calculator2.Calculator");
        options.setAutomationName("UiAutomator2");

        try {
            System.out.println("Attempting to initialize AndroidDriver with Appium...");
            System.out.println("Connecting to Appium server at: http://127.0.0.1:4723/");
            driver = new AndroidDriver(new URI("http://127.0.0.1:4723/").toURL(), options);

            System.out.println("Driver initialized successfully.");
        } catch (MalformedURLException e) {
            System.err.println("Malformed URL for Appium server.");
            e.printStackTrace();
        } catch (URISyntaxException e) {
            System.err.println("Invalid URI syntax.");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error initializing AndroidDriver: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Driver initialization failed!");
        }

        Assertions.assertNotNull(driver, "Driver should not be null after initialization!");
    }


    @Test
    public void testAddition() {
        Assertions.assertNotNull(driver, "Driver was not initialized. Test aborted!");
        System.out.println("Running addition test...");
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
