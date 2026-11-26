package br.ifsp.demo.ui.tests;

import br.ifsp.demo.support.DbFixtures;
import br.ifsp.demo.support.SeedData;
import br.ifsp.demo.ui.UiTestBase;
import br.ifsp.demo.ui.pages.AppShell;
import br.ifsp.demo.ui.pages.MyActivitiesPage;
import br.ifsp.demo.ui.pages.StudentActivitiesPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("UiTest")
@DisplayName("UI - Tela minhas atividades (aluno)")
class MyActivitiesUiTest extends UiTestBase {
    private static final String SWIMMING = "NATAÇÃO";
    private static final DateTimeFormatter BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private MyActivitiesPage openEnrolledInSwimming() {
        StudentAccount student = registerStudent();
        enrollViaApi(student, SeedData.SWIMMING_CLASS);
        loginPage().loginAs(student.email(), student.password()).waitForPath("/aluno/atividades");
        return new MyActivitiesPage(driver).open();
    }

    @Test
    @DisplayName("mostra abas vazias para aluno sem matrícula")
    void shouldShowEmptyState() {
        loginAsNewStudent();

        MyActivitiesPage page = new MyActivitiesPage(driver).open();

        assertThat(page.tab(0)).isEqualTo("Ativas (0)");
        assertThat(page.tab(1)).isEqualTo("Histórico (0)");
        assertThat(page.panelText()).isEqualTo("Você não possui matrículas ativas.");
        assertThat(page.showHistory().panelText()).isEqualTo("Nenhuma atividade encerrada.");
    }

    @Test
    @DisplayName("mostra a matrícula ativa com data de início e mensalidade")
    void shouldShowActiveEnrollment() {
        MyActivitiesPage page = openEnrolledInSwimming();

        assertThat(page.tab(0)).isEqualTo("Ativas (1)");
        assertThat(page.cardText(SWIMMING))
                .contains(SeedData.TRAINER_JULIANA_NAME)
                .contains("Seg 11:30–12:30")
                .contains("Início: " + LocalDate.now().format(BR))
                .contains("R$ 180,00");
    }

    @Test
    @DisplayName("cancela a atividade e move para o histórico")
    void shouldCancelActivity() {
        LocalDate endDate = LocalDate.now().plusDays(7);
        MyActivitiesPage page = openEnrolledInSwimming().openCancel(SWIMMING);
        assertThat(page.cancelDialogText()).contains("Apenas a atividade Natação será encerrada");

        page.setEndDate(endDate).confirmCancel().waitDialogClosed();

        assertThat(page.toast()).isEqualTo("Matrícula encerrada.");
        assertThat(page.tab(0)).isEqualTo("Ativas (0)");
        assertThat(page.tab(1)).isEqualTo("Histórico (1)");
        assertThat(page.showHistory().cardText(SWIMMING)).contains("Encerrada em: " + endDate.format(BR));
        assertThat(DbFixtures.text("SELECT status FROM enrollment_activities")).isEqualTo("INACTIVE");
    }

    @Test
    @DisplayName("bloqueia data de cancelamento anterior à data de início")
    void shouldRejectDateBeforeStart() {
        MyActivitiesPage page = openEnrolledInSwimming().openCancel(SWIMMING)
                .setEndDate(LocalDate.now().minusDays(1)).confirmCancel();

        assertThat(page.dialogError()).isEqualTo("A data de encerramento não pode ser anterior à data de início.");
        assertThat(DbFixtures.text("SELECT status FROM enrollment_activities")).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("mantém a atividade ativa ao fechar o diálogo com Voltar")
    void shouldGoBackFromDialog() {
        MyActivitiesPage page = openEnrolledInSwimming().openCancel(SWIMMING).backFromDialog();

        assertThat(page.tab(0)).isEqualTo("Ativas (1)");
    }

    @Test
    @DisplayName("libera a vaga para a atividade ser escolhida de novo")
    void shouldAllowEnrollingAgainAfterCancel() {
        MyActivitiesPage page = openEnrolledInSwimming().openCancel(SWIMMING).confirmCancel().waitDialogClosed();
        assertThat(page.tab(1)).isEqualTo("Histórico (1)");

        new AppShell(driver).navigateTo("Atividades disponíveis");
        StudentActivitiesPage available = new StudentActivitiesPage(driver).waitUntilLoaded();

        assertThat(available.badge(SWIMMING)).isEqualTo("20 vagas");
    }

    @Test
    @DisplayName("sugere a data local de hoje ao cancelar à noite")
    void shouldSuggestLocalDate() {
        StudentAccount student = registerStudent();
        enrollViaApi(student, SeedData.SWIMMING_CLASS);
        simulateBrowserClock(Instant.parse("2026-11-26T01:30:00Z"), "America/Sao_Paulo");
        loginPage().loginAs(student.email(), student.password()).waitForPath("/aluno/atividades");

        MyActivitiesPage page = new MyActivitiesPage(driver).open().openCancel(SWIMMING);

        assertThat(page.endDate()).isEqualTo("2026-11-25");
    }

    @Test
    @DisplayName("mantém Minhas atividades sem rolagem horizontal no celular")
    void shouldBeResponsive() {
        MyActivitiesPage page = openEnrolledInSwimming();
        useMobileScreen();

        assertThat(page.hasHorizontalScroll()).isFalse();
        assertThat(page.openCancel(SWIMMING).cancelDialogText()).containsIgnoringCase("Cancelar matrícula");
    }
}
