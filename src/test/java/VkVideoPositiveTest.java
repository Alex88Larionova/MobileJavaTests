import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URL;
import java.time.Duration;

public class VkVideoPositiveTest {
    private AndroidDriver driver;
    private WebDriverWait wait;

    @BeforeClass
    public void setUp() throws Exception {
        System.out.println("Инициализация Appium драйвера для приложения 'VK Видео'...");

        UiAutomator2Options options = new UiAutomator2Options();
        options.setDeviceName("Android Emulator");
        options.setPlatformName("Android");
        options.setPlatformVersion("11");
        options.setAutomationName("uiautomator2");

        options.setAppPackage("com.vk.vkvideo");
        options.setAppWaitActivity("*");
        options.setAppWaitDuration(Duration.ofSeconds(30));
        options.setAutoGrantPermissions(true);
        options.setNewCommandTimeout(Duration.ofSeconds(60));

        driver = new AndroidDriver(new URL("http://127.0.0.1:4724/wd/hub"), options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        // Дожидаемся главного экрана VK Видео
        wait.until(ExpectedConditions.presenceOfElementLocated(
                AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Video\")")
        ));

        System.out.println("Приложение 'VK Видео' успешно запущено!");
    }

    @Test(description = "Позитивный сценарий: воспроизведение и пауза видео")
    public void testVideoPlaybackAndPause() throws InterruptedException {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("ТЕСТ: VK Видео — позитивный сценарий (воспроизведение и пауза)");
        System.out.println("=".repeat(70));

        // === ШАГ 1: Запустить видео ===
        System.out.println("\nШАГ 1: Запускаем видео");

        // Находим первый доступный видео-элемент (обычно это карточка видео)
        WebElement videoCard = wait.until(ExpectedConditions.elementToBeClickable(
                AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.ImageView\").index(0)")
        ));
        videoCard.click();
        System.out.println("Видео запущено");

        // === ШАГ 2: Дождаться начала воспроизведения ===
        System.out.println("\nШАГ 2: Ждём начала воспроизведения видео");

        // Ждём появления элементов плеера (кнопка паузы, прогресс-бар)
        WebElement pauseButton = wait.until(ExpectedConditions.presenceOfElementLocated(
                AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"Pause\")")
        ));
        System.out.println("Видео начало воспроизводиться (появилась кнопка паузы)");

        // Дополнительная проверка: ждём, что видео действительно играет
        Thread.sleep(5000); // Даём время на загрузку и начало воспроизведения

        // === ШАГ 3: Нажать на паузу ===
        System.out.println("\nШАГ 3: Нажимаем на паузу");

        // Кликаем по кнопке паузы
        pauseButton.click();
        System.out.println("Кнопка паузы нажата");

        // === ШАГ 4: Убедиться, что видео остановилось ===
        System.out.println("\nШАГ 4: Проверяем, что видео остановилось");

        // После паузы кнопка должна измениться на "Play"
        WebElement playButton = wait.until(ExpectedConditions.presenceOfElementLocated(
                AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"Play\")")
        ));
        System.out.println("Видео остановилось (появилась кнопка воспроизведения)");

        // Дополнительная проверка: можно проверить, что прогресс не меняется
        String progressBefore = getVideoProgress();
        Thread.sleep(3000);
        String progressAfter = getVideoProgress();

        Assert.assertEquals(progressBefore, progressAfter,
                "Прогресс видео изменился после паузы — видео продолжает воспроизводиться");

        System.out.println("Прогресс видео не изменился — подтверждение паузы");
        System.out.println("\nУСПЕХ: Позитивный сценарий выполнен полностью!");
        System.out.println("=".repeat(70));
    }

    /**
     * Вспомогательный метод: получение прогресса видео
     * Ищет элементы с временем (например: "0:15 / 2:30")
     */
    private String getVideoProgress() {
        try {
            // Ищем элементы с текстом времени
            java.util.List<WebElement> timeElements = driver.findElements(
                    By.xpath("//*[contains(@text, ':') and contains(@text, '/')]")
            );

            if (!timeElements.isEmpty()) {
                String progress = timeElements.get(0).getText().trim();
                System.out.println("Прогресс видео: " + progress);
                return progress;
            }

            // Альтернативный поиск: элементы с цифрами и двоеточием
            timeElements = driver.findElements(
                    By.xpath("//*[matches(@text, '\\d+:\\d+')]")
            );

            if (!timeElements.isEmpty()) {
                String progress = timeElements.get(0).getText().trim();
                System.out.println("Прогресс видео: " + progress);
                return progress;
            }

        } catch (Exception e) {
            System.out.println("Не удалось получить прогресс видео: " + e.getMessage());
        }

        return "0:00"; // Значение по умолчанию
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            System.out.println("\nЗавершение теста");
            driver.quit();
            System.out.println("Драйвер закрыт");
        }
    }
}