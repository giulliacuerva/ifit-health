package br.ifsp.demo.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportsPage extends BasePage {
    public static final String TITLE = "Relatórios — iFit Health";

    private static final By DATES = By.cssSelector("form input[type='date']");
    private static final By SELECTS = By.cssSelector("form button[role='combobox']");
    private static final By GENERATE = By.cssSelector("form button[type='submit']");
    private static final By ROWS = By.cssSelector("tbody tr");
    private static final By EMPTY = By.xpath("//td[contains(., 'Nenhuma atividade no período')]");
    private static final By PERIOD = By.xpath("//p[contains(., 'Período:')]");

    public ReportsPage(WebDriver driver) {
        super(driver);
    }

    public ReportsPage open() {
        openPath("/admin/relatorios");
        return waitUntilLoaded();
    }

    public ReportsPage visit() {
        openPath("/admin/relatorios");
        return this;
    }

    public ReportsPage waitUntilLoaded() {
        waitForPath("/admin/relatorios");
        waitForTitle(TITLE);
        wait.until(ExpectedConditions.numberOfElementsToBe(DATES, 2));
        return this;
    }

    public ReportsPage setPeriod(LocalDate start, LocalDate end) {
        List<WebElement> dates = driver.findElements(DATES);
        setDate(dates.get(0), start.toString());
        setDate(dates.get(1), end.toString());
        return this;
    }

    public String startDate() {
        return driver.findElements(DATES).get(0).getDomProperty("value");
    }

    public String endDate() {
        return driver.findElements(DATES).get(1).getDomProperty("value");
    }

    public ReportsPage chooseSport(String sport) {
        choose(driver.findElements(SELECTS).get(0), sport);
        return this;
    }

    public ReportsPage chooseWeekday(String weekday) {
        choose(driver.findElements(SELECTS).get(1), weekday);
        return this;
    }

    public ReportsPage generate() {
        click(GENERATE);
        return this;
    }

    public ReportsPage waitForResult() {
        wait.until(ExpectedConditions.presenceOfElementLocated(PERIOD));
        return this;
    }

    public String period() {
        return visible(PERIOD).getText();
    }

    public String stat(String label) {
        WebElement box = visible(By.xpath("//div[text()='" + label + "']/.."));
        return box.findElements(By.tagName("div")).get(1).getText();
    }

    private List<WebElement> rows() {
        List<WebElement> rows = new ArrayList<>();
        for (WebElement row : driver.findElements(ROWS)) {
            if (row.findElements(By.tagName("td")).size() > 1) {
                rows.add(row);
            }
        }
        return rows;
    }

    public List<String> activities() {
        List<String> activities = new ArrayList<>();
        for (WebElement row : rows()) {
            activities.add(row.findElement(By.tagName("td")).getText());
        }
        return activities;
    }

    public String cell(int row, int column) {
        return rows().get(row).findElements(By.tagName("td")).get(column).getText();
    }

    public boolean isEmpty() {
        return !driver.findElements(EMPTY).isEmpty();
    }
}
