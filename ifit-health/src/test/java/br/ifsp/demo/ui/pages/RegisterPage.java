package br.ifsp.demo.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.time.LocalDate;

public class RegisterPage extends BasePage {
    public static final String TITLE = "Cadastro de aluno — iFit Health";

    private static final By NAME = By.id("name");
    private static final By LASTNAME = By.id("lastname");
    private static final By CPF = By.id("cpf");
    private static final By BIRTHDATE = By.id("birth");
    private static final By GENDER = By.id("gender");
    private static final By PHONE = By.id("tel");
    private static final By EMAIL = By.id("email");
    private static final By PASSWORD = By.id("pw");
    private static final By CONFIRM = By.id("pw2");
    private static final By ADDRESS_SWITCH = By.cssSelector("button[role='switch']");
    private static final By CEP = By.id("cep");
    private static final By NUMBER = By.id("number");
    private static final By STREET = By.id("street");
    private static final By CITY = By.id("city");
    private static final By STATE = By.id("state");
    private static final By SUBMIT = By.cssSelector("button[type='submit']");
    private static final By BACK_TO_LOGIN = By.partialLinkText("Voltar ao login");

    public RegisterPage(WebDriver driver) {
        super(driver);
    }

    public RegisterPage open() {
        openPath("/cadastro");
        return waitUntilLoaded();
    }

    public RegisterPage waitUntilLoaded() {
        visible(NAME);
        waitForTitle(TITLE);
        return this;
    }

    public RegisterPage fillName(String name, String lastname) {
        type(NAME, name);
        type(LASTNAME, lastname);
        return this;
    }

    public RegisterPage fillCpf(String cpf) {
        type(CPF, cpf);
        return this;
    }

    public String cpfValue() {
        return visible(CPF).getDomProperty("value");
    }

    public String birthdateValidationMessage() {
        return visible(BIRTHDATE).getDomProperty("validationMessage");
    }

    public RegisterPage fillBirthdate(LocalDate birthdate) {
        setDate(visible(BIRTHDATE), birthdate.toString());
        return this;
    }

    public RegisterPage chooseGender(String gender) {
        choose(visible(GENDER), gender);
        return this;
    }

    public RegisterPage fillPhone(String phone) {
        type(PHONE, phone);
        return this;
    }

    public RegisterPage fillAccess(String email, String password, String confirmation) {
        type(EMAIL, email);
        type(PASSWORD, password);
        type(CONFIRM, confirmation);
        return this;
    }

    public RegisterPage enableAddress() {
        click(ADDRESS_SWITCH);
        visible(CEP);
        return this;
    }

    public boolean isAddressVisible() {
        return !driver.findElements(CEP).isEmpty();
    }

    public RegisterPage fillAddress(String cep, String number, String street, String city, String state) {
        type(CEP, cep);
        type(NUMBER, number);
        type(STREET, street);
        type(CITY, city);
        type(STATE, state);
        return this;
    }

    public String cepValue() {
        return visible(CEP).getDomProperty("value");
    }

    public RegisterPage submit() {
        click(SUBMIT);
        return this;
    }

    public LoginPage backToLogin() {
        click(BACK_TO_LOGIN);
        return new LoginPage(driver);
    }
}
