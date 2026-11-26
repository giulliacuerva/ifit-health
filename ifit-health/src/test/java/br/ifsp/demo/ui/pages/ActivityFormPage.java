package br.ifsp.demo.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class ActivityFormPage extends BasePage {
    public static final String NEW_TITLE = "Nova atividade — iFit Health";
    public static final String EDIT_TITLE = "Editar atividade — iFit Health";

    private static final By SELECTS = By.cssSelector("form button[role='combobox']");
    private static final By HOURS = By.cssSelector("select[aria-label='Hora']");
    private static final By MINUTES = By.cssSelector("select[aria-label='Minuto']");
    private static final By NUMBERS = By.cssSelector("form input[type='number']");
    private static final By SUBMIT = By.cssSelector("form button[type='submit']");
    private static final By API_ERROR = By.cssSelector("form div.text-destructive div.font-semibold");
    private static final By AFFECTED = By.cssSelector("form div.text-destructive div.rounded-md");

    public ActivityFormPage(WebDriver driver) {
        super(driver);
    }

    public ActivityFormPage openNew() {
        openPath("/admin/atividades/nova");
        return waitUntilLoaded();
    }

    public ActivityFormPage openEdit(String id) {
        openPath("/admin/atividades/" + id);
        return waitUntilLoaded();
    }

    public ActivityFormPage visitEdit(String id) {
        openPath("/admin/atividades/" + id);
        return this;
    }

    public ActivityFormPage waitUntilLoaded() {
        wait.until(ExpectedConditions.numberOfElementsToBe(SELECTS, 3));
        wait.until(ExpectedConditions.or(ExpectedConditions.titleIs(NEW_TITLE), ExpectedConditions.titleIs(EDIT_TITLE)));
        return this;
    }

    private WebElement select(int index) {
        return driver.findElements(SELECTS).get(index);
    }

    public ActivityFormPage chooseSport(String sport) {
        choose(select(0), sport);
        return this;
    }

    public ActivityFormPage chooseRoom(String room) {
        choose(select(1), room);
        return this;
    }

    public ActivityFormPage chooseTrainer(String trainer) {
        choose(select(2), trainer);
        return this;
    }

    public String selectedSport() {
        return select(0).getText();
    }

    public String selectedRoom() {
        return select(1).getText();
    }

    public String selectedTrainer() {
        return select(2).getText();
    }

    public ActivityFormPage toggleWeekday(String weekday) {
        click(buttonWithText(weekday));
        return this;
    }

    public ActivityFormPage setStart(String time) {
        return setTime(0, time);
    }

    public ActivityFormPage setEnd(String time) {
        return setTime(1, time);
    }

    private ActivityFormPage setTime(int index, String time) {
        String[] parts = time.split(":");
        chooseNative(driver.findElements(HOURS).get(index), parts[0]);
        chooseNative(driver.findElements(MINUTES).get(index), parts[1]);
        return this;
    }

    public String startTime() {
        return selectedValue(driver.findElements(HOURS).get(0)) + ":" + selectedValue(driver.findElements(MINUTES).get(0));
    }

    private static String selectedValue(WebElement select) {
        return select.getDomProperty("value");
    }

    public ActivityFormPage setCapacity(String capacity) {
        WebElement input = driver.findElements(NUMBERS).get(0);
        input.clear();
        input.sendKeys(capacity);
        return this;
    }

    public String capacity() {
        return driver.findElements(NUMBERS).get(0).getDomProperty("value");
    }

    public ActivityFormPage setMonthlyFee(String fee) {
        WebElement input = driver.findElements(NUMBERS).get(1);
        input.clear();
        input.sendKeys(fee);
        return this;
    }

    public String monthlyFee() {
        return driver.findElements(NUMBERS).get(1).getDomProperty("value");
    }

    public ActivityFormPage fill(String sport, String room, String trainer, List<String> weekdays,
                                 String start, String end, String capacity, String fee) {
        chooseSport(sport);
        chooseRoom(room);
        chooseTrainer(trainer);
        for (String weekday : weekdays) {
            toggleWeekday(weekday);
        }
        setStart(start);
        setEnd(end);
        setCapacity(capacity);
        return setMonthlyFee(fee);
    }

    public ActivityFormPage submit() {
        click(SUBMIT);
        return this;
    }

    public AdminActivitiesPage submitAndReturnToList() {
        submit();
        return new AdminActivitiesPage(driver).waitUntilLoaded();
    }

    public AdminActivitiesPage cancel() {
        click(buttonWithText("Cancelar"));
        return new AdminActivitiesPage(driver).waitUntilLoaded();
    }

    public ActivityFormPage waitForFeedback() {
        wait.until(d -> hasFormErrors() || !d.findElements(API_ERROR).isEmpty());
        return this;
    }

    public String apiError() {
        return visible(API_ERROR).getText();
    }

    public List<String> affectedStudents() {
        wait.until(ExpectedConditions.presenceOfElementLocated(AFFECTED));
        return texts(AFFECTED);
    }
}
