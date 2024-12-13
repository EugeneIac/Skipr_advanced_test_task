import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.AppiumBy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
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

            startWatcherForUnresponsivePopup(driver);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(120));

            int firstNum = 3;
            int secondNum = 5;
            String plusButton = "plus";
            String equalsButton = "equals";
            String additionResultField = "com.google.android.calculator:id/result_final";
            int expectedSum = 8;

            handleCloseAppDialog(driver);
            pressNumber(wait, firstNum);
            wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.accessibilityId(plusButton))).click();
            System.out.println("Pressed '+' button.");

            handleCloseAppDialog(driver);
            pressNumber(wait, secondNum);
            wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.accessibilityId(equalsButton))).click();
            System.out.println("Pressed '=' button.");

            handleCloseAppDialog(driver);
            WebElement resultElement = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(AppiumBy.id(additionResultField))
            );
            String resultText = resultElement.getText();
            int actualResult = Integer.parseInt(resultText);

            System.out.println("Addition Result: " + actualResult);
            Assertions.assertEquals(expectedSum, actualResult, "Addition result is incorrect!");
        } catch (Exception e) {
            e.printStackTrace();
            savePageSource(driver);
            Assertions.fail("Test failed due to an exception: " + e.getMessage());
        } finally {
            if (driver != null) {
                System.out.println("Quitting driver...");
                driver.quit();
            }
        }
    }

    private void pressNumber(WebDriverWait wait, int number) throws IOException, InterruptedException {
        try {
            System.out.println("Verifying Calculator app is running...");
            for (int i = 0; i < 5; i++) {
                ProcessBuilder checkAppProcess = new ProcessBuilder(
                        "adb", "shell", "pidof", "com.google.android.calculator");
                checkAppProcess.redirectErrorStream(true);
                Process process = checkAppProcess.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String output = reader.readLine();

                if (output != null && !output.isEmpty()) {
                    System.out.println("Calculator app is running. Proceeding to press numbers.");
                    break;
                } else {
                    System.out.println("Calculator app is not running yet. Retrying...");
                    TimeUnit.SECONDS.sleep(20);
                }

                if (i == 4) {
                    throw new RuntimeException("Calculator app is not running after multiple attempts.");
                }
            }

            String numStr = String.valueOf(number);
            for (char digit : numStr.toCharArray()) {
                WebElement numberButton = wait.until(
                        ExpectedConditions.elementToBeClickable(AppiumBy.id("com.google.android.calculator:id/digit_" + number))
                );
                numberButton.click();
                System.out.println("Pressed number: " + digit);
            }
        } catch (Exception e) {
            System.err.println("Failed to press number: " + number + ", Error: " + e.getMessage());
            throw e;
        }
    }

    private void savePageSource(AndroidDriver driver) {
        try {
            if (driver != null) {
                String pageSource = driver.getPageSource();
                Path destPath = Path.of("screenshots", "page_source_failure.xml");
                Files.createDirectories(destPath.getParent());
                Files.writeString(destPath, pageSource);
                System.out.println("Page source saved at: " + destPath.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Failed to save page source: " + e.getMessage());
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

    private void handleCloseAppDialog(AndroidDriver driver) {
        try {
            System.out.println("Checking for 'Process system isn't responding' dialog...");
            // Wait up to 5 seconds to check if the "Close app" button appears
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement closeAppButton = wait.until(
                    ExpectedConditions.presenceOfElementLocated(AppiumBy.id("android:id/aerr_close"))
            );

            if (closeAppButton.isDisplayed()) {
                closeAppButton.click();
                System.out.println("'Close app' button clicked to dismiss the system dialog.");
            }
        } catch (Exception e) {
            System.out.println("No 'Process system isn't responding' dialog found.");
        }
    }

    private void startWatcherForUnresponsivePopup(AndroidDriver driver) {
        new Thread(() -> {
            try {
                while (true) {
                    System.out.println("Watcher: Checking for unresponsive popup...");
                    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));

                    try {
                        // Look for the popup containing "isn't responding"
                        WebElement popup = wait.until(ExpectedConditions.presenceOfElementLocated(
                                AppiumBy.xpath("//*[contains(@text, \"isn't responding\")]")
                        ));

                        if (popup != null) {
                            System.out.println("Watcher: Unresponsive popup detected!");

                            // Click the "Close app" button
                            WebElement closeAppButton = driver.findElement(AppiumBy.id("android:id/aerr_close"));
                            closeAppButton.click();
                            System.out.println("Watcher: 'Close app' button clicked.");
                        }
                    } catch (Exception e) {
                        // Silently handle absence of popup
                    }

                    Thread.sleep(2000); // Check every 2 seconds
                }
            } catch (InterruptedException e) {
                System.out.println("Watcher thread interrupted.");
            }
        }).start();
    }


}
