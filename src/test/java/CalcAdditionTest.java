import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.AppiumBy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class CalcAdditionTest {

    @Test
    public void testAddition() {
        AndroidDriver driver = null;

        try {
            System.out.println("Setting up driver...");

            waitForEmulatorAndApp();

            ensureAppiumSettingsRunning();

            UiAutomator2Options options = new UiAutomator2Options();
            options.setPlatformName("Android");
            options.setUdid("emulator-5554");
            options.setAppPackage("com.google.android.calculator");
            options.setAppActivity("com.android.calculator2.Calculator");
            options.setAutomationName("UiAutomator2");

            options.setUiautomator2ServerInstallTimeout(Duration.ofSeconds(300));
            options.setUiautomator2ServerLaunchTimeout(Duration.ofSeconds(300));
            options.setAppWaitDuration(Duration.ofSeconds(300));
            options.setAdbExecTimeout(Duration.ofSeconds(300));
            options.setNewCommandTimeout(Duration.ofSeconds(300));

            driver = new AndroidDriver(new URI("http://127.0.0.1:4723/").toURL(), options);
            Assertions.assertNotNull(driver, "Driver was not initialized");
            System.out.println("Driver initialized successfully!");

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(120));

            int firstNum = 3;
            int secondNum = 5;
            String plusButton = "plus";
            String equalsButton = "equals";
            String additionResultField = "com.google.android.calculator:id/result_final";
            int expectedSum = 8;

            pressNumber(wait, firstNum);

            wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.accessibilityId(plusButton))).click();

            pressNumber(wait, secondNum);

            wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.accessibilityId(equalsButton))).click();

            WebElement resultElement = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(AppiumBy.id(additionResultField))
            );
            String resultText = resultElement.getText();
            int actualResult = Integer.parseInt(resultText);

            System.out.println("Addition Result: " + actualResult);
            Assertions.assertEquals(expectedSum, actualResult, "Addition result is incorrect!");

        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Test failed due to an exception: " + e.getMessage());
        } finally {
            if (driver != null) {
                System.out.println("Quitting driver...");
                driver.quit();
            }
        }
    }

    private void pressNumber(WebDriverWait wait, int number) {
        String numStr = String.valueOf(number);
        for (char digit : numStr.toCharArray()) {
            wait.until(ExpectedConditions.elementToBeClickable(
                    AppiumBy.accessibilityId(String.valueOf(digit))
            )).click();
        }
    }

    private void waitForEmulatorAndApp() throws Exception {
        System.out.println("Waiting for emulator to be ready...");
        boolean emulatorReady = false;

        for (int i = 0; i < 12; i++) {
            StringBuilder output = getStringBuilder();

            if (output.toString().contains("stopped")) {
                emulatorReady = true;
                break;
            }

            System.out.println("Emulator still booting... Retrying in 10 seconds...");
            TimeUnit.SECONDS.sleep(10);
        }

        if (!emulatorReady) {
            throw new RuntimeException("Emulator failed to boot in time!");
        }

        System.out.println("Emulator is ready. Waiting for app to launch...");
        TimeUnit.SECONDS.sleep(10);
    }

    private void ensureAppiumSettingsRunning() throws IOException, InterruptedException {
        System.out.println("Launching Appium Settings app...");

        ProcessBuilder launchSettings = new ProcessBuilder(
                "adb", "shell", "am", "start", "-n", "io.appium.settings/.Settings");
        launchSettings.redirectErrorStream(true);
        Process launchProcess = launchSettings.start();
        launchProcess.waitFor();

        if (launchProcess.exitValue() == 0) {
            System.out.println("Appium Settings app launched successfully.");
        } else {
            System.err.println("Failed to launch Appium Settings app. Please check the logs.");
            throw new RuntimeException("Failed to launch Appium Settings app.");
        }
        TimeUnit.SECONDS.sleep(120);
    }



    private static StringBuilder getStringBuilder() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("adb", "shell", "getprop", "init.svc.bootanim");
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line);
        }
        return output;
    }

}
