package br.ifsp.demo.ui.tests;

import br.ifsp.demo.support.DbFixtures;
import br.ifsp.demo.support.FakeData;
import br.ifsp.demo.support.SeedData;
import br.ifsp.demo.ui.UiTestBase;
import br.ifsp.demo.ui.pages.ActivityFormPage;
import br.ifsp.demo.ui.pages.AdminActivitiesPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Tag("UiTest")
@DisplayName("UI - Tela de edição de atividade")
class EditActivityUiTest extends UiTestBase {
    private ActivityFormPage openSwimming() {
        loginAsAdmin();
        return new AdminActivitiesPage(driver).waitUntilLoaded().edit(SeedData.SWIMMING_NAME);
    }

    @Test
    @DisplayName("carrega os dados atuais da atividade na edição")
    void shouldLoadCurrentData() {
        ActivityFormPage form = openSwimming();

        assertThat(form.selectedSport()).isEqualTo(SeedData.SWIMMING_NAME);
        assertThat(form.selectedRoom()).startsWith("Piscina");
        assertThat(form.selectedTrainer()).isEqualTo(SeedData.TRAINER_JULIANA_NAME);
        assertThat(form.startTime()).isEqualTo("11:30");
        assertThat(form.capacity()).isEqualTo("20");
        assertThat(form.monthlyFee()).isEqualTo("180");
    }

    @Test
    @DisplayName("salva nova capacidade e mostra na lista")
    void shouldEditCapacity() {
        int capacity = FakeData.faker().number().numberBetween(1, 20);

        AdminActivitiesPage list = openSwimming().setCapacity(String.valueOf(capacity)).submitAndReturnToList();

        assertThat(list.toast()).isEqualTo("Atividade atualizada!");
        assertThat(list.cell(SeedData.SWIMMING_NAME, 4)).isEqualTo("0/" + capacity);
    }

    @Test
    @DisplayName("mostra a nova mensalidade ao admin depois de editar")
    void shouldEditFee() {
        AdminActivitiesPage list = openSwimming().setMonthlyFee("215.50").submitAndReturnToList();

        assertThat(list.cell(SeedData.SWIMMING_NAME, 5)).isEqualTo("R$ 215,50");
    }

    @Test
    @DisplayName("mostra o erro do back-end quando a capacidade é menor que os matriculados")
    void shouldRejectCapacityBelowEnrolled() {
        DbFixtures.fillActivity(SeedData.SWIMMING_CLASS, SeedData.SWIMMING_FEE, 2);

        ActivityFormPage form = openSwimming().setCapacity("1").submit();

        assertThat(form.apiError()).isEqualTo("New capacity cannot be lower than enrolled students: 2");
    }

    @Test
    @DisplayName("lista os alunos afetados quando o novo horário gera conflito")
    void shouldShowAffectedStudents() {
        UUID gym = DbFixtures.insertActivity(SeedData.SPORT_WEIGHT_TRAINING, SeedData.ROOM_GYM, SeedData.TRAINER_JOHN,
            LocalTime.of(8, 0), LocalTime.of(9, 0), 10, new BigDecimal("120"), true, DayOfWeek.WEDNESDAY);
        String student = FakeData.firstName() + " " + FakeData.lastName();
        UUID enrollment = DbFixtures.insertEnrollment(DbFixtures.insertCustomer(student, FakeData.cpf(), FakeData.email()), true);
        DbFixtures.insertEnrollmentActivity(enrollment, SeedData.SWIMMING_CLASS, SeedData.SWIMMING_FEE, LocalDate.now(), null, true);
        DbFixtures.insertEnrollmentActivity(enrollment, gym, new BigDecimal("120"), LocalDate.now(), null, true);
        loginAsAdmin();

        ActivityFormPage form = new ActivityFormPage(driver).openEdit(gym.toString())
            .toggleWeekday("Quarta").toggleWeekday("Segunda").setStart("12:00").setEnd("13:00").submit();

        assertThat(form.apiError()).isEqualTo("Student has a schedule conflict");
        assertThat(form.affectedStudents()).singleElement().asString()
            .contains(student).contains(SeedData.SWIMMING_NAME).contains("11:30–12:30");
    }

    @Test
    @DisplayName("mostra erro ao editar atividade que não existe")
    void shouldShowErrorForUnknownActivity() {
        loginAsAdmin();
        UUID unknown = UUID.randomUUID();

        ActivityFormPage page = new ActivityFormPage(driver).visitEdit(unknown.toString());

        assertThat(page.errorBox()).isEqualTo("Activity class not found: " + unknown);
    }

    @Test
    @DisplayName("volta para a lista sem salvar ao cancelar a edição")
    void shouldCancel() {
        AdminActivitiesPage list = openSwimming().setCapacity("5").cancel();

        assertThat(list.cell(SeedData.SWIMMING_NAME, 4)).isEqualTo("0/20");
    }

    @Test
    @DisplayName("mantém o formulário de edição sem rolagem horizontal no celular")
    void shouldBeResponsive() {
        ActivityFormPage form = openSwimming();
        useMobileScreen();

        assertThat(form.hasHorizontalScroll()).isFalse();
    }
}
