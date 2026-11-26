package br.ifsp.demo.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class AdminActivitiesPage extends BasePage {
    public static final String TITLE = "Gerenciar atividades — iFit Health";

    private static final By ROWS = By.cssSelector("tbody tr");
    private static final By EMPTY = By.xpath("//td[contains(., 'Nenhuma atividade')]");
    private static final By NEW_ACTIVITY = By.partialLinkText("Nova atividade");

    public AdminActivitiesPage(WebDriver driver) {
        super(driver);
    }

    public AdminActivitiesPage open() {
        openPath("/admin/atividades");
        return waitUntilLoaded();
    }

    public AdminActivitiesPage visit() {
        openPath("/admin/atividades");
        return this;
    }

    public AdminActivitiesPage waitUntilLoaded() {
        waitForPath("/admin/atividades");
        waitForTitle(TITLE);
        wait.until(d -> !rows().isEmpty() || !d.findElements(EMPTY).isEmpty());
        return this;
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

    private WebElement findRow(String sport) {
        for (WebElement row : rows()) {
            if (row.findElement(By.tagName("td")).getText().equals(sport)) {
                return row;
            }
        }
        return null;
    }

    private WebElement row(String sport) {
        return wait.until(d -> findRow(sport));
    }

    public List<String> sports() {
        List<String> sports = new ArrayList<>();
        for (WebElement row : rows()) {
            sports.add(row.findElement(By.tagName("td")).getText());
        }
        return sports;
    }

    public int rowCount() {
        return rows().size();
    }

    public String cell(String sport, int column) {
        return row(sport).findElements(By.tagName("td")).get(column).getText();
    }

    public ActivityFormPage clickNewActivity() {
        click(NEW_ACTIVITY);
        return new ActivityFormPage(driver).waitUntilLoaded();
    }

    public ActivityFormPage edit(String sport) {
        row(sport).findElement(By.partialLinkText("Editar")).click();
        return new ActivityFormPage(driver).waitUntilLoaded();
    }
}
