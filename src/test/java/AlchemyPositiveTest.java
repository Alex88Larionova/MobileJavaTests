import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URL;
import java.time.Duration;
import java.util.List;

public class AlchemyPositiveTest {
    private AndroidDriver driver;
    private WebDriverWait wait;

    @BeforeClass
    public void setUp() throws Exception {
        System.out.println("Инициализация Appium драйвера для приложения 'Алхимия'...");

        UiAutomator2Options options = new UiAutomator2Options();
        options.setDeviceName("Android Emulator");
        options.setPlatformName("Android");
        options.setPlatformVersion("11");
        options.setAutomationName("uiautomator2");

        options.setAppPackage("com.ilyin.alchemy");
        options.setAppWaitActivity("*");
        options.setAppWaitDuration(Duration.ofSeconds(30));
        options.setAutoGrantPermissions(true);
        options.setNewCommandTimeout(Duration.ofSeconds(60));

        options.setCapability("enforceXPath1", true);
        options.setCapability("waitForIdleTimeout", 10000);

        driver = new AndroidDriver(new URL("http://127.0.0.1:4724/wd/hub"), options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(150)); // таймаут в условиях медленного эмулятора, при достаточно производительной машине необходимо уменишить

        wait.until(ExpectedConditions.presenceOfElementLocated(
                AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Play\")")
        ));

        System.out.println("✅ Приложение 'Алхимия' успешно запущено!");
    }

    @Test(description = "Тест: Получение подсказки за просмотр рекламы (шаги 1-4 из ТЗ)")
    public void testGetHintAfterWatchingAd() throws InterruptedException {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("ТЕСТ: Алхимия — получение подсказки за просмотр рекламы");
        System.out.println("=".repeat(70));

        // ШАГ 1: Нажать на кнопку "Играть"
        System.out.println("\nШАГ 1: Нажимаем кнопку 'Играть'");
        WebElement playButton = wait.until(ExpectedConditions.elementToBeClickable(
                AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Play\")")
        ));
        playButton.click();
        System.out.println("Кнопка 'Играть' нажата");

        // ШАГ 2: Нажать на кнопку подсказок (лампочка)
        System.out.println("\nШАГ 2: Нажимаем кнопку подсказок (лампочка)");
        WebElement hintButton = wait.until(ExpectedConditions.elementToBeClickable(
                AppiumBy.androidUIAutomator("new UiSelector().className(\"android.view.View\").instance(5)")
        ));
        hintButton.click();
        System.out.println("Кнопка подсказок (лампочка) нажата");

        // ШАГ 3: Ждём завершения лоадера и нажимаем кнопку "Watch"
        System.out.println("\nШАГ 3: Ждём завершения лоадера и нажимаем кнопку 'Watch'...");

        int hintsBefore = getHintCount();
        System.out.println("Количество подсказок ДО: " + hintsBefore);

        wait.until(ExpectedConditions.presenceOfElementLocated(
                AppiumBy.androidUIAutomator("new UiSelector().text(\"Your hints\")")
        ));
        System.out.println("Окно подсказок появилось");

        WebElement watchButton = wait.until(ExpectedConditions.presenceOfElementLocated(
                AppiumBy.androidUIAutomator("new UiSelector().text(\"Watch\")")
        ));
        System.out.println("Кнопка 'Watch' появилась — лоадер завершён!");

        watchButton.click();
        System.out.println("Клик выполнен по кнопке 'Watch'");

        // ШАГ 4: Мониторинг рекламы БЕЗ кликов по автоматической рекламе
        System.out.println("\nШАГ 4: Мониторинг рекламы БЕЗ кликов по автоматической рекламе...");
        System.out.println("Ждём появления интерактивных элементов (стрелок или крестика)");

        monitorAdWithoutClicking();

        System.out.println("Ждём возврата в игровой экран...");
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    AppiumBy.androidUIAutomator("new UiSelector().className(\"android.view.View\").instance(5)")
            ));
            System.out.println("Успешно вернулись в игровой экран");
        } catch (Exception e) {
            System.out.println("Не удалось подтвердить возврат в игру");
        }

        int hintsAfter = getHintCount();
        System.out.println("📊 Количество подсказок ПОСЛЕ: " + hintsAfter);

        Assert.assertEquals(hintsAfter - hintsBefore, 2,
                "Количество подсказок должно увеличиться ровно на 2");

        System.out.println("✅✅✅ УСПЕХ: Подсказки увеличились на 2!");
        System.out.println("   • Было: " + hintsBefore);
        System.out.println("   • Стало: " + hintsAfter);
        System.out.println("=".repeat(70));
    }

    /**
     * Мониторинг рекламы БЕЗ кликов по автоматической рекламе
     * Только ждём появления интерактивных элементов (стрелок или крестика)
     */
    private void monitorAdWithoutClicking() throws InterruptedException {
        System.out.println("Мониторим появление интерактивных элементов рекламы...");

        String[] interactiveLocators = {
                // Крестик (pageIndex: 3) - основной элемент завершения
                "//android.widget.RelativeLayout[@content-desc=\"pageIndex: 3\"]/android.widget.FrameLayout/android.widget.FrameLayout/android.view.ViewGroup/android.view.ViewGroup/android.view.ViewGroup[1]/android.widget.ImageView",

                // Первая стрелка (pageIndex: 1)
                "//android.widget.RelativeLayout[@content-desc=\"pageIndex: 1\"]/android.widget.FrameLayout/android.widget.FrameLayout/android.view.ViewGroup/android.view.ViewGroup/android.view.ViewGroup[2]/android.view.ViewGroup[1]/android.view.ViewGroup[2]/android.view.ViewGroup[2]/android.widget.ImageView",

                // Вторая стрелка (pageIndex: 2)
                "//android.widget.RelativeLayout[@content-desc=\"pageIndex: 2\"]/android.widget.FrameLayout/android.widget.FrameLayout/android.view.ViewGroup/android.view.ViewGroup/android.view.ViewGroup[2]/android.view.ViewGroup[2]/android.view.ViewGroup[1]/android.view.ViewGroup/android.view.ViewGroup[2]/android.widget.ImageView"
        };

        String[] locatorNames = {
                "Крестик (pageIndex: 3) - завершение рекламы",
                "Первая стрелка (pageIndex: 1)",
                "Вторая стрелка (pageIndex: 2)"
        };

        boolean adCompleted = false;
        int attempts = 0;
        final int MAX_ATTEMPTS = 25;

        while (!adCompleted && attempts < MAX_ATTEMPTS) {
            attempts++;
            System.out.println("   Попытка " + attempts + "/" + MAX_ATTEMPTS + ": поиск интерактивных элементов...");

            for (int i = 0; i < interactiveLocators.length; i++) {
                try {
                    WebElement interactiveElement = driver.findElement(org.openqa.selenium.By.xpath(interactiveLocators[i]));
                    if (interactiveElement.isDisplayed() && interactiveElement.isEnabled()) {
                        System.out.println("Найден интерактивный элемент: " + locatorNames[i]);

                        org.openqa.selenium.Point location = interactiveElement.getLocation();
                        int centerX = location.getX() + interactiveElement.getSize().getWidth() / 2;
                        int centerY = location.getY() + interactiveElement.getSize().getHeight() / 2;

                        performReliableClick(centerX, centerY);
                        System.out.println("Клик выполнен по интерактивному элементу (" + centerX + ", " + centerY + ")");

                        Thread.sleep(5000);

                        if (i == 0) { // pageIndex: 3 (крестик)
                            adCompleted = true;
                            System.out.println("Реклама полностью завершена (нажат крестик)");
                        } else {
                            System.out.println("Продолжаем мониторинг - возможно появление крестика");
                        }
                        break;
                    }
                } catch (Exception e) {
                    // продолжаем поиск следующего локатора
                }
            }

            if (!adCompleted) {
                Thread.sleep(5000);
            }
        }

        if (adCompleted) {
            System.out.println("Реклама успешно завершена");
        } else {
            System.out.println("Интерактивные элементы не найдены — реклама завершилась автоматически");
        }
    }

    /**
     * Метод для клика по координатам
     */
    private void performReliableClick(int x, int y) throws InterruptedException {
        try {
            org.openqa.selenium.interactions.PointerInput finger =
                    new org.openqa.selenium.interactions.PointerInput(
                            org.openqa.selenium.interactions.PointerInput.Kind.TOUCH, "finger");
            org.openqa.selenium.interactions.Sequence tap =
                    new org.openqa.selenium.interactions.Sequence(finger, 1);

            tap.addAction(finger.createPointerMove(java.time.Duration.ofMillis(0),
                    org.openqa.selenium.interactions.PointerInput.Origin.viewport(), x, y));
            tap.addAction(finger.createPointerDown(org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
            tap.addAction(finger.createPointerUp(org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));

            driver.perform(java.util.Arrays.asList(tap));
        } catch (Exception e) {
            System.out.println("Ошибка при выполнении клика: " + e.getMessage());
            Thread.sleep(1000);
        }
    }

    private int getHintCount() {
        System.out.println("\nПоиск количества подсказок...");

        try {
            if (driver == null) {
                System.out.println("Драйвер не инициализирован");
                return 0;
            }

            List<WebElement> elements = driver.findElements(
                    AppiumBy.androidUIAutomator("new UiSelector().textMatches(\"^[1-6]$\")")
            );

            for (WebElement el : elements) {
                String text = el.getText().trim();
                System.out.println("   Элемент: '" + text + "'");

                if (text.matches("^[1-6]$")) {
                    int count = Integer.parseInt(text);
                    System.out.println("Найдено количество подсказок: " + count);
                    return count;
                }
            }

        } catch (Exception e) {
            System.out.println("Ошибка поиска: " + e.getMessage());
            if (e.getMessage().contains("session is either terminated") || e.getMessage().contains("not started")) {
                System.out.println("Сессия Appium завершена");
                return -1;
            }
        }

        System.out.println("Не удалось определить количество — возвращаем 0");
        return 0;
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            System.out.println("\nЗавершение теста: закрытие драйвера...");
            driver.quit();
            System.out.println("Драйвер закрыт");
        }
    }
}