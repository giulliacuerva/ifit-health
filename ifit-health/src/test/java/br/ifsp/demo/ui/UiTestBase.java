package br.ifsp.demo.ui;

import br.ifsp.demo.support.FakeData;
import br.ifsp.demo.support.IntegrationTestBase;
import br.ifsp.demo.support.SeedData;
import br.ifsp.demo.ui.pages.LoginPage;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class UiTestBase extends IntegrationTestBase {
    public static final String BASE_URL = System.getProperty("ui.baseUrl", "http://localhost:3000");

    protected ChromeDriver driver;

    public record StudentAccount(String name, String lastname, String email, String password) {
    }

    @BeforeEach
    public void setUp() {
        assumeTrue(frontEndIsRunning(), "O front-end não está rodando em " + BASE_URL + " (rode npm run dev em ifit-front)");
        ChromeOptions options = new ChromeOptions();
        if (Boolean.parseBoolean(System.getProperty("ui.headless", "true"))) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--lang=pt-BR");
        driver = new ChromeDriver(options);
        driver.manage().window().setSize(new Dimension(1366, 900));
        driver.get(BASE_URL);
        JavascriptExecutor js = driver;
        js.executeScript("localStorage.clear(); localStorage.setItem('ifit.apiUrl', arguments[0]);", "http://localhost:" + port);
        driver.navigate().refresh();
        setInitialPage();
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected void setInitialPage() {
    }

    protected void useMobileScreen() {
        driver.manage().window().setSize(new Dimension(390, 844));
    }

    protected void simulateBrowserClock(Instant now, String timeZone) {
        driver.executeCdpCommand("Emulation.setTimezoneOverride", Map.of("timezoneId", timeZone));
        String script = """
                (() => {
                  const fixed = %d;
                  const RealDate = Date;
                  class FrozenDate extends RealDate {
                    constructor(...args) { if (args.length === 0) { super(fixed); } else { super(...args); } }
                    static now() { return fixed; }
                  }
                  FrozenDate.UTC = RealDate.UTC;
                  FrozenDate.parse = RealDate.parse;
                  window.Date = FrozenDate;
                })();
                """.formatted(now.toEpochMilli());
        driver.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", Map.of("source", script));
    }

    protected List<String> enrollViaApi(StudentAccount student, UUID... activityIds) {
        String token = given().port(port).contentType(ContentType.JSON)
                .body(Map.of("username", student.email(), "password", student.password()))
                .when().post("/api/v1/authenticate")
                .then().statusCode(200).extract().path("token");
        return given().port(port).contentType(ContentType.JSON).header("Authorization", "Bearer " + token)
                .body(Map.of("activityClassIds", List.of(activityIds)))
                .when().post("/api/v1/me/enrollment")
                .then().statusCode(201).extract().path("activities.enrollmentActivityId");
    }

    protected LoginPage loginPage() {
        return new LoginPage(driver).open();
    }

    protected void loginAsAdmin() {
        loginPage().loginAs(SeedData.ADMIN_EMAIL, SeedData.ADMIN_PASSWORD).waitForPath("/admin/atividades");
    }

    protected StudentAccount loginAsNewStudent() {
        StudentAccount student = registerStudent();
        loginPage().loginAs(student.email(), student.password()).waitForPath("/aluno/atividades");
        return student;
    }

    protected StudentAccount registerStudent() {
        Map<String, Object> body = FakeData.registerRequest();
        given().port(port).contentType(ContentType.JSON).body(body)
                .when().post("/api/v1/register")
                .then().statusCode(201);
        return new StudentAccount((String) body.get("name"), (String) body.get("lastname"),
                (String) body.get("email"), (String) body.get("password"));
    }

    private static boolean frontEndIsRunning() {
        for (String host : List.of("127.0.0.1", "::1")) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(InetAddress.getByName(host), URI.create(BASE_URL).getPort()), 2000);
                return true;
            } catch (IOException ignored) {
            }
        }
        return false;
    }
}
