import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.AppiumBy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;

import java.net.URI;

public class CalcAdditionTest {

    @Test
    public void testAddition() {
        AndroidDriver driver = null;  // Declare driver here

        try {
            System.out.println("Setting up driver...");

            // Set up options for the AndroidDriver
            UiAutomator2Options options = new UiAutomator2Options();
            options.setPlatformName("Android");
            options.setUdid("emulator-5554");
            options.setAppPackage("com.google.android.calculator");
            options.setAppActivity("com.android.calculator2.Calculator");
            options.setAutomationName("UiAutomator2");

            // Initialize the AndroidDriver
            driver = new AndroidDriver(new URI("http://127.0.0.1:4723/").toURL(), options);
            Assertions.assertNotNull(driver, "Driver was not initialized");
            System.out.println("Driver initialized successfully!");

            // Test variables
            int firstNum = 3;
            int secondNum = 5;
            String plusButton = "plus";
            String equalsButton = "equals";
            String additionResultField = "com.google.android.calculator:id/result_final";
            int expectedSum = 8;

            // Test steps
            pressNumber(driver, firstNum);
            driver.findElement(AppiumBy.accessibilityId(plusButton)).click();
            pressNumber(driver, secondNum);
            driver.findElement(AppiumBy.accessibilityId(equalsButton)).click();

            // Verify result
            WebElement resultElement = driver.findElement(AppiumBy.id(additionResultField));
            String resultText = resultElement.getText();
            int actualResult = Integer.parseInt(resultText);

            System.out.println("Addition Result: " + actualResult);
            Assertions.assertEquals(expectedSum, actualResult, "Addition result is incorrect!");

        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Test failed due to an exception: " + e.getMessage());
        } finally {
            // Clean up driver to ensure the session ends
            if (driver != null) {
                System.out.println("Quitting driver...");
                driver.quit();
            }
        }
    }

    // Helper method to press numbers on the calculator
    private void pressNumber(AndroidDriver driver, int number) {
        String numStr = String.valueOf(number);
        for (char digit : numStr.toCharArray()) {
            driver.findElement(AppiumBy.accessibilityId(String.valueOf(digit))).click();
        }
    }
}
