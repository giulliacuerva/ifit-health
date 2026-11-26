package br.ifsp.demo.ui.tests;

import br.ifsp.demo.support.FakeData;
import br.ifsp.demo.support.SeedData;
import br.ifsp.demo.ui.UiTestBase;
import br.ifsp.demo.ui.pages.AdminActivitiesPage;
import br.ifsp.demo.ui.pages.AppShell;
import br.ifsp.demo.ui.pages.LoginPage;
import br.ifsp.demo.ui.pages.MyActivitiesPage;
import br.ifsp.demo.ui.pages.RegisterPage;
import br.ifsp.demo.ui.pages.ReportsPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("UiTest")
@DisplayName("UI - Tela de login")
class LoginUiTest extends UiTestBase {
    @Test
    @DisplayName("exibe o formulário de login")
    void shouldDisplayLoginFormElements() {
        LoginPage login = loginPage();

        assertThat(login.pageTitle()).isEqualTo(LoginPage.TITLE);
        assertThat(login.isFormDisplayed()).isTrue();
        assertThat(login.submitText()).isEqualTo("Entrar");
    }

    @Test
    @DisplayName("abre a área do admin ao entrar como administrador")
    void shouldLoginAsAdmin() {
        loginAsAdmin();

        AppShell shell = new AppShell(driver);
        assertThat(shell.currentPath()).isEqualTo("/admin/atividades");
        assertThat(shell.area()).isEqualTo("ADMINISTRAÇÃO");
        assertThat(shell.toast()).isEqualTo("Bem-vindo(a), Administrador!");
    }

    @Test
    @DisplayName("abre a área do aluno ao entrar como aluno")
    void shouldLoginAsStudent() {
        StudentAccount student = loginAsNewStudent();

        AppShell shell = new AppShell(driver);
        assertThat(shell.area()).isEqualTo("ÁREA DO ALUNO");
        assertThat(shell.loggedUser()).isEqualTo(student.name() + " " + student.lastname());
    }

    @Test
    @DisplayName("mostra erro e continua no login com senha errada")
    void shouldRejectWrongPassword() {
        LoginPage login = loginPage().loginAs(SeedData.ADMIN_EMAIL, FakeData.password());

        assertThat(login.toast()).isEqualTo("Sessão expirada ou credenciais inválidas.");
        assertThat(login.currentPath()).isEqualTo("/");
    }

    @Test
    @DisplayName("não envia e-mail inválido no login (validação do navegador)")
    void shouldValidateEmailFormat() {
        LoginPage login = loginPage().loginAs("aluno-sem-arroba", FakeData.password());

        assertThat(login.emailValidationMessage()).isNotBlank();
        assertThat(login.currentPath()).isEqualTo("/");
    }

    @Test
    @DisplayName("vai para o cadastro pelo link da tela de login")
    void shouldGoToRegister() {
        RegisterPage register = loginPage().goToRegister();

        assertThat(register.pageTitle()).isEqualTo(RegisterPage.TITLE);
        assertThat(register.currentPath()).isEqualTo("/cadastro");
        LoginPage login = register.backToLogin();
        login.waitForPath("/");
        assertThat(login.isFormDisplayed()).isTrue();
    }

    @Test
    @DisplayName("redireciona usuário logado da tela de login para a área dele")
    void shouldRedirectLoggedUser() {
        loginAsAdmin();

        new LoginPage(driver).visit().waitForPath("/admin/atividades");
    }

    @Test
    @DisplayName("sai da conta e bloqueia a área do admin depois")
    void shouldLogout() {
        loginAsAdmin();

        LoginPage login = new AppShell(driver).logout();
        assertThat(login.isFormDisplayed()).isTrue();

        new AdminActivitiesPage(driver).visit();
        login.waitForPath("/");
    }

    @Test
    @DisplayName("redireciona para o login ao abrir tela protegida sem sessão")
    void shouldProtectScreensWithoutSession() {
        new MyActivitiesPage(driver).visit();

        new LoginPage(driver).waitForPath("/");
    }

    @Test
    @DisplayName("manda de volta para a área do aluno quem abre tela de admin")
    void shouldBlockAdminScreensForStudents() {
        loginAsNewStudent();

        new ReportsPage(driver).visit();

        new AppShell(driver).waitForPath("/aluno/atividades");
    }

    @Test
    @DisplayName("adapta o login ao celular escondendo o banner e sem rolagem horizontal")
    void shouldBeResponsive() {
        LoginPage login = loginPage();
        assertThat(login.isHeroDisplayed()).isTrue();

        useMobileScreen();

        assertThat(login.isHeroDisplayed()).isFalse();
        assertThat(login.isFormDisplayed()).isTrue();
        assertThat(login.hasHorizontalScroll()).isFalse();
    }
}
