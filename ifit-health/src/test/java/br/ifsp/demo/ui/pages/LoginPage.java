package br.ifsp.demo.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class LoginPage extends BasePage {
    public static final String TITLE = "Entrar — iFit Health";

    private static final By EMAIL = By.id("email");
    private static final By PASSWORD = By.id("pw");
    private static final By SUBMIT = By.cssSelector("button[type='submit']");
    private static final By REGISTER_LINK = By.linkText("Cadastre-se");
    private static final By HERO = By.xpath("//h1[contains(., 'Mova-se.')]");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage open() {
        openPath("/");
        visible(EMAIL);
        waitForTitle(TITLE);
        return this;
    }

    public LoginPage visit() {
        openPath("/");
        return this;
    }

    public LoginPage fillEmail(String email) {
        type(EMAIL, email);
        return this;
    }

    public LoginPage fillPassword(String password) {
        type(PASSWORD, password);
        return this;
    }

    public LoginPage submit() {
        click(SUBMIT);
        return this;
    }

    public LoginPage loginAs(String email, String password) {
        fillEmail(email);
        fillPassword(password);
        return submit();
    }

    public RegisterPage goToRegister() {
        click(REGISTER_LINK);
        return new RegisterPage(driver).waitUntilLoaded();
    }

    public boolean isFormDisplayed() {
        return visible(EMAIL).isDisplayed() && visible(PASSWORD).isDisplayed() && visible(SUBMIT).isDisplayed();
    }

    public String submitText() {
        return visible(SUBMIT).getText();
    }

    public boolean isHeroDisplayed() {
        for (WebElement hero : driver.findElements(HERO)) {
            if (hero.isDisplayed()) {
                return true;
            }
        }
        return false;
    }

    public String emailValidationMessage() {
        return visible(EMAIL).getDomProperty("validationMessage");
    }
}
