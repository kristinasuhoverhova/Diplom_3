import com.stellarburger.client.UserApi;
import com.stellarburger.model.User;
import com.stellarburger.page.AccountPage;
import com.stellarburger.page.HeaderPage;
import com.stellarburger.page.LoginPage;
import com.stellarburger.page.MainPage;
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

import static org.junit.Assert.assertTrue;

public class PersonalAccountTest {
    WebDriver driver;
    User user;
    UserApi userApi = new UserApi();
    MainPage mainPage = new MainPage();
    AccountPage accountPage = new AccountPage();
    HeaderPage headerPage = new HeaderPage();
    LoginPage loginPage = new LoginPage();
    Waiting waiting = new Waiting();

    @Before
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.get("https://stellarburgers.nomoreparties.site/");
        // Заранее регистрируем пользователя через апи
        user = new User("kri_smile99@yandex.ru", "kri-smile99", "kristy");
        userApi.createUser(user);
    }

    @Test
    @DisplayName("Проверка перехода по логотипу личного кабинета")
    public void getIntoPersonalAccountWithHeaderButton() {
        headerPage.clickPersonalAccount(driver);
        loginPage.makeSignIn(driver, user.getEmail(), user.getPassword());
        headerPage.clickPersonalAccount(driver);
        waiting.waitVisibilityOfExitButton(driver, 1);

        assertTrue(accountPage.isExitButtonVisible(driver));
    }

    @Test
    @DisplayName("Проверка перехода по логотипу стеллар бургер")
    public void getIntoMainPageFromPersonalAccountWithLogoBurger() {
        headerPage.clickPersonalAccount(driver);
        loginPage.makeSignIn(driver, user.getEmail(), user.getPassword());
        headerPage.clickPersonalAccount(driver);
        waiting.waitVisibilityOfExitButton(driver, 1);
        headerPage.clickStellarBurgers(driver);

        assertTrue(mainPage.isMakeOrderButtonVisible(driver));
    }

    @Test
    @DisplayName("Проверка перехода по логотипу контструктора")
    public void getIntoMainPageFromPersonalAccountWithLogoConstructor() {
        headerPage.clickPersonalAccount(driver);
        loginPage.makeSignIn(driver, user.getEmail(), user.getPassword());
        headerPage.clickPersonalAccount(driver);
        waiting.waitVisibilityOfExitButton(driver, 1);
        headerPage.clickConstructor(driver);

        assertTrue(mainPage.isMakeOrderButtonVisible(driver));
    }

    @Test
    @DisplayName("Проверка выхода из личного кабинета")
    public void exitFromPersonalAccount() {
        headerPage.clickPersonalAccount(driver);
        loginPage.makeSignIn(driver, user.getEmail(), user.getPassword());
        headerPage.clickPersonalAccount(driver);
        waiting.waitVisibilityOfExitButton(driver, 2);
        accountPage.clickExitButton(driver);
        waiting.waitVisibilityOfEmailField(driver, 1);

        assertTrue(loginPage.isSignInButtonVisible(driver));
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