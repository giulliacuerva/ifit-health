package br.ifsp.demo.ui.pages;

import br.ifsp.demo.ui.UiTestBase;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class BasePage {
    protected final WebDriver driver;
    protected final WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public String pageTitle() {
        return driver.getTitle();
    }

    protected void openPath(String path) {
        driver.get(UiTestBase.BASE_URL + path);
    }

    protected void waitForTitle(String title) {
        wait.until(ExpectedConditions.titleIs(title));
    }

    public String currentPath() {
        return URI.create(driver.getCurrentUrl()).getPath();
    }

    public void waitForPath(String path) {
        wait.until(d -> currentPath().equals(path));
    }

    protected WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    protected void type(By locator, String text) {
        WebElement element = visible(locator);
        element.clear();
        element.sendKeys(text);
    }

    protected void setDate(WebElement input, String date) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("""
                const setter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value').set;
                setter.call(arguments[0], arguments[1]);
                arguments[0].dispatchEvent(new Event('input', { bubbles: true }));
                arguments[0].dispatchEvent(new Event('change', { bubbles: true }));
                """, input, date);
    }

    protected static By buttonWithText(String text) {
        return By.xpath("//button[text()='" + text + "']");
    }

    protected void choose(WebElement combobox, String optionText) {
        combobox.click();
        click(By.xpath("//*[@role='option'][contains(., '" + optionText + "')]"));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("[role='listbox']")));
    }

    protected void chooseNative(WebElement select, String value) {
        new Select(select).selectByValue(value);
    }

    protected List<String> texts(By locator) {
        List<String> texts = new ArrayList<>();
        for (WebElement element : driver.findElements(locator)) {
            texts.add(element.getText().trim());
        }
        return texts;
    }

    public List<String> formErrors() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("ul.list-disc li")));
        return texts(By.cssSelector("ul.list-disc li"));
    }

    public boolean hasFormErrors() {
        return !driver.findElements(By.cssSelector("ul.list-disc li")).isEmpty();
    }

    public String toast() {
        return visible(By.cssSelector("[data-sonner-toast] [data-title]")).getText();
    }

    public String errorBox() {
        return visible(By.cssSelector("div.rounded-lg.text-destructive")).getText();
    }

    public boolean hasHorizontalScroll() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Object result = js.executeScript("return document.documentElement.scrollWidth > window.innerWidth + 1;");
        return Boolean.TRUE.equals(result);
    }

    public String heading() {
        return visible(By.tagName("h1")).getText();
    }
}
