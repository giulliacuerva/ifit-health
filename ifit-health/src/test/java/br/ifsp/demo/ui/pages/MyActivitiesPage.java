package br.ifsp.demo.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.LocalDate;
import java.util.List;

public class MyActivitiesPage extends BasePage {
    public static final String TITLE = "Minhas atividades — iFit Health";

    private static final By TABS = By.cssSelector("[role='tab']");
    private static final By ACTIVE_PANEL = By.cssSelector("[role='tabpanel'][data-state='active']");
    private static final By CARD_TITLES = By.cssSelector("[role='tabpanel'][data-state='active'] h3");
    private static final By DIALOG = By.cssSelector("[role='dialog']");
    private static final By END_DATE = By.id("end");

    public MyActivitiesPage(WebDriver driver) {
        super(driver);
    }

    public MyActivitiesPage open() {
        openPath("/aluno/minhas");
        return waitUntilLoaded();
    }

    public MyActivitiesPage visit() {
        openPath("/aluno/minhas");
        return this;
    }

    public MyActivitiesPage waitUntilLoaded() {
        waitForPath("/aluno/minhas");
        waitForTitle(TITLE);
        wait.until(ExpectedConditions.numberOfElementsToBe(TABS, 2));
        return this;
    }

    public String tab(int index) {
        return driver.findElements(TABS).get(index).getText();
    }

    public MyActivitiesPage showActive() {
        driver.findElements(TABS).get(0).click();
        return this;
    }

    public MyActivitiesPage showHistory() {
        WebElement history = driver.findElements(TABS).get(1);
        history.click();
        wait.until(ExpectedConditions.attributeToBe(history, "data-state", "active"));
        return this;
    }

    public List<String> sports() {
        return texts(CARD_TITLES);
    }

    public String panelText() {
        return visible(ACTIVE_PANEL).getText();
    }

    private WebElement findCard(String sport) {
        for (WebElement title : driver.findElements(CARD_TITLES)) {
            if (title.getText().equalsIgnoreCase(sport)) {
                return title.findElement(By.xpath("../../.."));
            }
        }
        return null;
    }

    private WebElement card(String sport) {
        return wait.until(d -> findCard(sport));
    }

    public String cardText(String sport) {
        return card(sport).getText();
    }

    public MyActivitiesPage openCancel(String sport) {
        card(sport).findElement(By.xpath(".//button[text()='Cancelar']")).click();
        visible(DIALOG);
        return this;
    }

    public String cancelDialogText() {
        return visible(DIALOG).getText();
    }

    public String endDate() {
        return visible(END_DATE).getDomProperty("value");
    }

    public MyActivitiesPage setEndDate(LocalDate date) {
        setDate(visible(END_DATE), date.toString());
        return this;
    }

    public MyActivitiesPage confirmCancel() {
        click(By.xpath("//*[@role='dialog']//button[contains(., 'Confirmar cancelamento')]"));
        return this;
    }

    public MyActivitiesPage waitDialogClosed() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(DIALOG));
        return this;
    }

    public MyActivitiesPage backFromDialog() {
        click(By.xpath("//*[@role='dialog']//button[text()='Voltar']"));
        return waitDialogClosed();
    }

    public String dialogError() {
        return visible(By.cssSelector("[role='dialog'] div.text-destructive")).getText();
    }
}
