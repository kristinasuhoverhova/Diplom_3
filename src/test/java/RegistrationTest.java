import com.stellarburger.client.UserApi;
import com.stellarburger.model.User;
import com.stellarburger.page.LoginPage;
import com.stellarburger.page.MainPage;
import com.stellarburger.page.RegisterPage;
import com.stellarburger.util.Waiting;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Wait;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RegistrationTest {
    WebDriver driver;
    User user;
    UserApi userApi = new UserApi();
    MainPage mainPage = new MainPage();
    RegisterPage registerPage = new RegisterPage();
    LoginPage loginPage = new LoginPage();
    Waiting waiting = new Waiting();

    @Before
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
        driver.get("https://stellarburgers.nomoreparties.site/");
        driver.manage().window().maximize();
        user = new User("kri_smile99@yandex.ru", "kri-smile99", "kristy");
    }

    @Test
    @DisplayName("Проверка успешной регистрации")
    public void successfulRegisterTest() {
        mainPage.clickLoginButton(driver);
        waiting.waitVisibilityOfRegisterButton(driver, 1);
        loginPage.clickRegisterButton(driver);
        waiting.waitVisibilityOfEmailField(driver, 1);
        registerPage.makeRegistration(driver, user.getName(), user.getEmail(), user.getPassword());
        waiting.waitVisibilityOfRegisterButton(driver, 1);

        assertEquals(driver.getCurrentUrl(), "https://stellarburgers.nomoreparties.site/login");
        loginPage.makeSignIn(driver, user.getEmail(), user.getPassword());
        waiting.waitVisibilityOfMakeOrderButton(driver, 1);

        assertTrue(mainPage.isMakeOrderButtonVisible(driver));
    }

    @Test
    @DisplayName("Проверка появления дополнительного блока с ошибкой при введении некорректного пароля")
    public void checkShortPasswordMakesErrorMessage() {
        mainPage.clickLoginButton(driver);
        waiting.waitVisibilityOfRegisterButton(driver, 1);
        loginPage.clickRegisterButton(driver);
        waiting.waitVisibilityOfEmailField(driver, 1);
        // Меняем пароль на некорректный (меньше 5 символов)
        user.setPassword("abc");
        registerPage.makeRegistration(driver, user.getName(), user.getEmail(), user.getPassword());

        assertTrue(registerPage.isIncorrectPasswordVisible(driver));
    }

    @After
    public void tearDown() {
        driver.quit();
        Response loginResponse = userApi.loginUser(user);
        String accessToken = loginResponse.then().extract().path("accessToken");
        if (accessToken != null) {
            userApi.deleteUser(accessToken);
        }
    }
}
