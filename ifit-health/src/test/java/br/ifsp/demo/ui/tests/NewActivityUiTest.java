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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("UiTest")
@DisplayName("UI - Tela de nova atividade")
class NewActivityUiTest extends UiTestBase {
    private ActivityFormPage form;
    @Override
    protected void setInitialPage() {
        loginAsAdmin();
        form = new ActivityFormPage(driver).openNew();
    }

    @Test
    @DisplayName("cria atividade válida e mostra na lista")
    void shouldCreateActivity() {
        int capacity = FakeData.faker().number().numberBetween(5, 31);
        int fee = FakeData.faker().number().numberBetween(80, 250);

        form.fill("Musculação", "Sala de Musculação", "John Doe", List.of("Terça", "Quinta"),
            "08:00", "09:00", String.valueOf(capacity), String.valueOf(fee));
        AdminActivitiesPage list = form.submitAndReturnToList();

        assertThat(list.toast()).isEqualTo("Atividade cadastrada!");
        assertThat(list.cell("Musculação", 3)).contains("Ter · Qui").contains("08:00–09:00");
        assertThat(list.cell("Musculação", 4)).isEqualTo("0/" + capacity);
        assertThat(list.cell("Musculação", 5)).isEqualTo("R$ " + fee + ",00");
        assertThat(DbFixtures.count("SELECT COUNT(*) FROM activity_class")).isEqualTo(2);
    }

    @Test
    @DisplayName("lista todos os campos obrigatórios com o formulário vazio")
    void shouldShowRequiredErrors() {
        form.submit();

        assertThat(form.formErrors()).containsExactly("Selecione a modalidade.",
            "Selecione a sala.",
            "Selecione o professor.",
            "Selecione ao menos um dia da semana.",
            "Informe horário de início e fim.",
            "Capacidade deve ser um número inteiro positivo.",
            "Mensalidade deve ser maior ou igual a zero.");
        assertThat(form.currentPath()).isEqualTo("/admin/atividades/nova");
    }

    @Test
    @DisplayName("rejeita horário final antes do inicial no formulário")
    void shouldRejectEndBeforeStart() {
        form.fill("Musculação", "Sala de Musculação", "John Doe", List.of("Sexta"), "10:00", "09:00", "10", "100").submit();

        assertThat(form.formErrors()).containsExactly("O horário de término deve ser após o início.");
    }

    @Test
    @DisplayName("rejeita capacidade maior que a da sala no formulário")
    void shouldRejectCapacityAboveRoom() {
        form.fill("Musculação", "Sala de Musculação", "John Doe", List.of("Sexta"), "08:00", "09:00", "31", "100").submit();

        assertThat(form.formErrors()).containsExactly("Capacidade excede o limite da sala (30).");
    }

    @Test
    @DisplayName("rejeita sala que não serve para a modalidade")
    void shouldRejectRoomTypeMismatch() {
        form.fill("Natação", "Sala de Musculação", "John Doe", List.of("Sexta"), "08:00", "09:00", "10", "100").submit();

        assertThat(form.formErrors()).containsExactly("A modalidade Natação exige sala do tipo Piscina.");
    }

    @Test
    @DisplayName("mostra o erro do back-end quando a sala já está ocupada")
    void shouldShowRoomConflictFromBackEnd() {
        form.fill("Natação", "Piscina", "Maria Silva", List.of("Segunda"), "12:00", "13:00", "10", "100").submit();

        assertThat(form.apiError()).isEqualTo("Room is already booked for the given schedule");
        assertThat(DbFixtures.count("SELECT COUNT(*) FROM activity_class")).isEqualTo(1);
    }

    @Test
    @DisplayName("valida no formulário, em português, o horário de funcionamento (06:00-22:00)")
    void shouldValidateOpeningHours() {
        form.fill("Musculação", "Sala de Musculação", "John Doe", List.of("Sexta"), "05:00", "06:00", "10", "100")
            .submit()
            .waitForFeedback();

        assertThat(form.hasFormErrors())
            .as("validação local esperada, mas o formulário só mostrou a mensagem do back-end: %s",
                form.hasFormErrors() ? "" : form.apiError()).isTrue();
    }

    @Test
    @DisplayName("volta para a lista sem salvar ao cancelar a nova atividade")
    void shouldCancel() {
        form.chooseSport("Judô");

        AdminActivitiesPage list = form.cancel();

        assertThat(list.sports()).containsExactly(SeedData.SWIMMING_NAME);
    }

    @Test
    @DisplayName("mantém o formulário de nova atividade sem rolagem horizontal no celular")
    void shouldBeResponsive() {
        useMobileScreen();

        form.fill("Musculação", "Sala de Musculação", "John Doe", List.of("Domingo"), "07:00", "08:00", "10", "99");

        assertThat(form.hasHorizontalScroll()).isFalse();
        assertThat(form.submitAndReturnToList().sports()).contains("Musculação");
    }
}
