package br.ifsp.demo.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.ArrayList;
import java.util.List;

public class StudentActivitiesPage extends BasePage {
    public static final String TITLE = "Atividades disponíveis — iFit Health";

    private static final By CARD_TITLES = By.cssSelector("main h3");
    private static final By EMPTY = By.xpath("//p[contains(., 'Nenhuma atividade disponível')]");
    private static final By SELECTION_BAR = By.cssSelector("div.fixed.bottom-0");
    private static final By DIALOG = By.cssSelector("[role='dialog']");

    public StudentActivitiesPage(WebDriver driver) {
        super(driver);
    }

    public StudentActivitiesPage open() {
        openPath("/aluno/atividades");
        return waitUntilLoaded();
    }

    public StudentActivitiesPage waitUntilLoaded() {
        waitForPath("/aluno/atividades");
        waitForTitle(TITLE);
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(CARD_TITLES),
                ExpectedConditions.presenceOfElementLocated(EMPTY)));
        return this;
    }

    public List<String> sports() {
        return texts(CARD_TITLES);
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

    public String badge(String sport) {
        return card(sport).findElement(By.cssSelector("div.pt-2 > :last-child")).getText();
    }

    public StudentActivitiesPage toggle(String sport) {
        card(sport).click();
        return this;
    }

    public boolean isSelected(String sport) {
        return card(sport).getDomAttribute("class").contains("ring-2");
    }

    public boolean isSelectionBarVisible() {
        return !driver.findElements(SELECTION_BAR).isEmpty();
    }

    public String selectionSummary() {
        return visible(By.cssSelector("div.fixed.bottom-0 > div > div")).getText();
    }

    public StudentActivitiesPage clearSelection() {
        click(By.xpath("//div[contains(@class,'fixed')]//button[text()='Limpar']"));
        return this;
    }

    public StudentActivitiesPage openConfirmation() {
        click(By.xpath("//div[contains(@class,'fixed')]//button[text()='Matricular']"));
        visible(DIALOG);
        return this;
    }

    public String dialogText() {
        return visible(DIALOG).getText();
    }

    public StudentActivitiesPage backFromDialog() {
        click(By.xpath("//*[@role='dialog']//button[text()='Voltar']"));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(DIALOG));
        return this;
    }

    public MyActivitiesPage confirm() {
        click(By.xpath("//*[@role='dialog']//button[contains(., 'Confirmar matrícula')]"));
        return new MyActivitiesPage(driver).waitUntilLoaded();
    }

    public StudentActivitiesPage confirmExpectingError() {
        click(By.xpath("//*[@role='dialog']//button[contains(., 'Confirmar matrícula')]"));
        return this;
    }

    public String dialogError() {
        return visible(By.cssSelector("[role='dialog'] div.text-destructive")).getText();
    }
}
