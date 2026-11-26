package br.ifsp.demo.ui.tests;

import br.ifsp.demo.support.DbFixtures;
import br.ifsp.demo.support.SeedData;
import br.ifsp.demo.ui.UiTestBase;
import br.ifsp.demo.ui.pages.ActivityFormPage;
import br.ifsp.demo.ui.pages.AdminActivitiesPage;
import br.ifsp.demo.ui.pages.AppShell;
import br.ifsp.demo.ui.pages.ReportsPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("UiTest")
@DisplayName("UI - Tela de atividades do admin")
class AdminActivitiesUiTest extends UiTestBase {
    @Test
    @DisplayName("mostra todas as colunas da atividade cadastrada")
    void shouldShowActivityColumns() {
        loginAsAdmin();
        AdminActivitiesPage page = new AdminActivitiesPage(driver).waitUntilLoaded();

        assertThat(page.heading()).isEqualToIgnoringCase("Atividades");
        assertThat(page.sports()).containsExactly(SeedData.SWIMMING_NAME);
        assertThat(page.cell(SeedData.SWIMMING_NAME, 1)).isEqualTo(SeedData.TRAINER_JULIANA_NAME);
        assertThat(page.cell(SeedData.SWIMMING_NAME, 2)).isEqualTo("Piscina");
        assertThat(page.cell(SeedData.SWIMMING_NAME, 3)).contains("Seg").contains("11:30–12:30");
        assertThat(page.cell(SeedData.SWIMMING_NAME, 4)).isEqualTo("0/20");
        assertThat(page.cell(SeedData.SWIMMING_NAME, 5)).isEqualTo("R$ 180,00");
        assertThat(page.cell(SeedData.SWIMMING_NAME, 6)).isEqualTo("Ativa");
    }

    @Test
    @DisplayName("mostra o número de matriculados e as atividades inativas")
    void shouldShowEnrollmentsAndStatus() {
        DbFixtures.fillActivity(SeedData.SWIMMING_CLASS, SeedData.SWIMMING_FEE, 2);
        DbFixtures.insertActivity(SeedData.SPORT_JUDO, SeedData.ROOM_JUDO, SeedData.TRAINER_CARLOS,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 10, new BigDecimal("90"), false, DayOfWeek.FRIDAY);

        loginAsAdmin();
        AdminActivitiesPage page = new AdminActivitiesPage(driver).waitUntilLoaded();

        assertThat(page.rowCount()).isEqualTo(2);
        assertThat(page.cell(SeedData.SWIMMING_NAME, 4)).isEqualTo("2/20");
        assertThat(page.cell("Judô", 6)).isEqualTo("Inativa");
    }

    @Test
    @DisplayName("abre o formulário de nova atividade e o de edição")
    void shouldNavigateToForms() {
        loginAsAdmin();
        AdminActivitiesPage page = new AdminActivitiesPage(driver).waitUntilLoaded();

        ActivityFormPage newForm = page.clickNewActivity();
        assertThat(newForm.pageTitle()).isEqualTo(ActivityFormPage.NEW_TITLE);
        assertThat(newForm.currentPath()).isEqualTo("/admin/atividades/nova");

        ActivityFormPage editForm = page.open().edit(SeedData.SWIMMING_NAME);
        assertThat(editForm.pageTitle()).isEqualTo(ActivityFormPage.EDIT_TITLE);
        assertThat(editForm.currentPath()).isEqualTo("/admin/atividades/" + SeedData.SWIMMING_CLASS);
    }

    @Test
    @DisplayName("navega para os relatórios pelo menu lateral")
    void shouldNavigateToReports() {
        loginAsAdmin();

        new AppShell(driver).navigateTo("Relatórios");

        ReportsPage reports = new ReportsPage(driver).waitUntilLoaded();
        assertThat(reports.pageTitle()).isEqualTo(ReportsPage.TITLE);
        assertThat(reports.heading()).isEqualToIgnoringCase("Relatórios");
    }

    @Test
    @DisplayName("mantém a lista de atividades sem rolagem horizontal no celular")
    void shouldBeResponsive() {
        loginAsAdmin();
        useMobileScreen();

        AdminActivitiesPage page = new AdminActivitiesPage(driver).open();

        assertThat(page.sports()).containsExactly(SeedData.SWIMMING_NAME);
        assertThat(page.hasHorizontalScroll()).isFalse();
    }
}
