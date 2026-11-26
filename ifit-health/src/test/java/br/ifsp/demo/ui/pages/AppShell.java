package br.ifsp.demo.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class AppShell extends BasePage {
    private static final By LOGOUT = By.xpath("//aside//button[contains(., 'Sair') or @aria-label='Sair']");

    public AppShell(WebDriver driver) {
        super(driver);
    }

    public void navigateTo(String menuLabel) {
        click(By.xpath("//aside//nav//a[contains(., '" + menuLabel + "')]"));
    }

    public String area() {
        return visible(By.cssSelector("aside .uppercase")).getText();
    }

    public String loggedUser() {
        return visible(By.cssSelector("aside .border-t .font-semibold")).getText();
    }

    public LoginPage logout() {
        for (WebElement button : driver.findElements(LOGOUT)) {
            if (button.isDisplayed()) {
                button.click();
                break;
            }
        }
        LoginPage login = new LoginPage(driver);
        login.waitForPath("/");
        return login;
    }
}
