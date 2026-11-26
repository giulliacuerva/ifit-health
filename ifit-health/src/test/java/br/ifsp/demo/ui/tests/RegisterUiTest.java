package br.ifsp.demo.ui.tests;

import br.ifsp.demo.support.DbFixtures;
import br.ifsp.demo.support.FakeData;
import br.ifsp.demo.support.SeedData;
import br.ifsp.demo.ui.UiTestBase;
import br.ifsp.demo.ui.pages.AppShell;
import br.ifsp.demo.ui.pages.RegisterPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("UiTest")
@DisplayName("UI - Tela de cadastro de aluno")
class RegisterUiTest extends UiTestBase {
    private RegisterPage fillValidForm(String email, String password) {
        return new RegisterPage(driver).open()
                .fillName(FakeData.firstName(), FakeData.lastName())
                .fillCpf(FakeData.cpf())
                .fillBirthdate(FakeData.birthdate())
                .chooseGender("Feminino")
                .fillPhone(FakeData.phone())
                .fillAccess(email, password, password);
    }

    @Test
    @DisplayName("cria a conta, entra e abre a área do aluno")
    void shouldRegisterStudent() {
        String email = FakeData.email();
        RegisterPage register = fillValidForm(email, FakeData.password()).submit();

        assertThat(register.toast()).isEqualTo("Conta criada com sucesso!");
        register.waitForPath("/aluno/atividades");
        assertThat(DbFixtures.text("SELECT role FROM app_user WHERE email = ?", email)).isEqualTo("USER");
    }

    @Test
    @DisplayName("cria a conta com endereço")
    void shouldRegisterStudentWithAddress() {
        String email = FakeData.email();
        String city = FakeData.faker().address().city();
        fillValidForm(email, FakeData.password())
                .enableAddress()
                .fillAddress("13560000", "100", FakeData.faker().address().streetName(), city, "sp")
                .submit()
                .waitForPath("/aluno/atividades");

        assertThat(DbFixtures.text("SELECT address_city || '/' || address_state || '/' || address_cep FROM customers WHERE email = ?", email))
                .isEqualTo(city + "/SP/13560-000");
    }

    @Test
    @DisplayName("lista todos os campos obrigatórios com o cadastro vazio")
    void shouldShowRequiredErrors() {
        RegisterPage register = new RegisterPage(driver).open().submit();

        assertThat(register.formErrors()).contains(
                "O nome é obrigatório.",
                "O sobrenome é obrigatório.",
                "Informe um e-mail válido.",
                "A senha deve ter entre 8 e 72 caracteres.",
                "Informe um CPF válido.",
                "A data de nascimento é obrigatória.",
                "O gênero é obrigatório.");
        assertThat(register.currentPath()).isEqualTo("/cadastro");
    }

    @Test
    @DisplayName("rejeita senha e confirmação diferentes")
    void shouldRejectDifferentPasswords() {
        RegisterPage register = fillValidForm(FakeData.email(), FakeData.password());
        register.fillAccess(FakeData.email(), "senhaForte123", "outraSenha123").submit();

        assertThat(register.formErrors()).containsExactly("As senhas não conferem.");
    }

    @Test
    @DisplayName("rejeita CPF com dígito verificador inválido na tela de cadastro")
    void shouldRejectInvalidCpf() {
        RegisterPage register = fillValidForm(FakeData.email(), FakeData.password()).fillCpf("12345678900").submit();

        assertThat(register.formErrors()).containsExactly("Informe um CPF válido.");
    }

    @Test
    @DisplayName("bloqueia data de nascimento no futuro (validação de data máxima do navegador)")
    void shouldRejectFutureBirthdate() {
        String email = FakeData.email();
        RegisterPage register = fillValidForm(email, FakeData.password())
                .fillBirthdate(FakeData.futureDate().plusDays(2)).submit();

        assertThat(register.birthdateValidationMessage()).isNotBlank();
        assertThat(register.currentPath()).isEqualTo("/cadastro");
        assertThat(DbFixtures.count("SELECT COUNT(*) FROM app_user WHERE email = ?", email)).isZero();
    }

    @Test
    @DisplayName("mostra a mensagem do back-end para e-mail já cadastrado")
    void shouldShowDuplicatedEmailError() {
        RegisterPage register = fillValidForm(SeedData.ANA_EMAIL, FakeData.password()).submit();

        assertThat(register.formErrors()).containsExactly("Este e-mail já está cadastrado.");
        assertThat(register.currentPath()).isEqualTo("/cadastro");
    }

    @Test
    @DisplayName("formata CPF e CEP enquanto digita")
    void shouldMaskCpfAndCep() {
        RegisterPage register = new RegisterPage(driver).open().fillCpf("52998224725").enableAddress()
                .fillAddress("13560001", "1", "Rua", "Cidade", "SP");

        assertThat(register.cpfValue()).isEqualTo("529.982.247-25");
        assertThat(register.cepValue()).isEqualTo("13560-001");
    }

    @Test
    @DisplayName("exige os campos de endereço só quando o endereço é ativado")
    void shouldValidateAddressWhenEnabled() {
        RegisterPage register = fillValidForm(FakeData.email(), FakeData.password());
        assertThat(register.isAddressVisible()).isFalse();

        register.enableAddress().submit();

        assertThat(register.formErrors()).containsExactlyInAnyOrder(
                "O CEP é obrigatório.",
                "O número do endereço é obrigatório.",
                "A rua é obrigatória.",
                "A cidade é obrigatória.",
                "Informe a sigla do estado com 2 letras.");
    }

    @Test
    @DisplayName("mantém o cadastro usável na tela de celular")
    void shouldBeResponsive() {
        useMobileScreen();

        String email = FakeData.email();
        fillValidForm(email, FakeData.password()).submit();

        AppShell shell = new AppShell(driver);
        shell.waitForPath("/aluno/atividades");
        assertThat(shell.hasHorizontalScroll()).isFalse();
    }

    @Test
    @DisplayName("deixa o cadastro sem rolagem horizontal no celular")
    void shouldFitMobileScreen() {
        useMobileScreen();

        RegisterPage register = new RegisterPage(driver).open().enableAddress();

        assertThat(register.hasHorizontalScroll()).isFalse();
    }
}
