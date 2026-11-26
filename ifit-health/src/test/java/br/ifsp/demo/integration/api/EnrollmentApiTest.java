package br.ifsp.demo.integration.api;

import br.ifsp.demo.support.ApiTestBase;
import br.ifsp.demo.support.DbFixtures;
import br.ifsp.demo.support.SeedData;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@Tag("ApiTest")
@Tag("IntegrationTest")
@DisplayName("API de matrícula: /me/enrollment")
class EnrollmentApiTest extends ApiTestBase {
    private static Map<String, Object> enrollIn(Object... activityIds) {
        return Map.of("activityClassIds", List.of(activityIds));
    }

    private UUID gymOnMonday(LocalTime start, LocalTime end, int capacity) {
        return DbFixtures.insertActivity(SeedData.SPORT_WEIGHT_TRAINING, SeedData.ROOM_GYM, SeedData.TRAINER_JOHN,
                start, end, capacity, new BigDecimal("120"), true, DayOfWeek.MONDAY);
    }

    private String enrollInSwimming(Student student) {
        return as(student).body(enrollIn(SeedData.SWIMMING_CLASS)).when().post("/me/enrollment")
                .then().statusCode(201)
                .extract().path("activities[0].enrollmentActivityId");
    }

    @Test
    @DisplayName("retorna 201 com a matrícula do aluno autenticado")
    void shouldEnrollStudent() {
        Student student = newStudent();

        as(student).body(enrollIn(SeedData.SWIMMING_CLASS)).when().post("/me/enrollment")
                .then().statusCode(201)
                .contentType(ContentType.JSON)
                .body("id", notNullValue())
                .body("customerId", equalTo(student.customerId().toString()))
                .body("active", is(true))
                .body("activities", hasSize(1))
                .body("activities[0].enrollmentActivityId", notNullValue())
                .body("activities[0].activityClassId", equalTo(SeedData.SWIMMING_CLASS.toString()))
                .body("activities[0].monthlyFee", isNumber(180))
                .body("activities[0].active", is(true))
                .body("activities[0].startDate", equalTo(LocalDate.now().toString()))
                .body("activities[0].endDate", nullValue());
    }

    @Test
    @DisplayName("matricula em várias atividades de uma vez e reduz as vagas")
    void shouldEnrollInSeveralActivities() {
        UUID gym = gymOnMonday(LocalTime.of(8, 0), LocalTime.of(9, 0), 10);

        as(newStudent()).body(enrollIn(SeedData.SWIMMING_CLASS, gym)).when().post("/me/enrollment")
                .then().statusCode(201)
                .body("activities.activityClassId", containsInAnyOrder(SeedData.SWIMMING_CLASS.toString(), gym.toString()));

        asAdmin().when().get("/activity-classes")
                .then().body("find { it.id == '%s' }.availableSpots".formatted(gym), equalTo(9));
    }

    @Test
    @DisplayName("adiciona nova atividade a uma matrícula existente")
    void shouldAddActivityToExistingEnrollment() {
        Student student = newStudent();
        enrollInSwimming(student);
        UUID gym = gymOnMonday(LocalTime.of(8, 0), LocalTime.of(9, 0), 10);

        as(student).body(enrollIn(gym)).when().post("/me/enrollment")
                .then().statusCode(201)
                .body("activities", hasSize(2));

        assertThat(DbFixtures.count("SELECT COUNT(*) FROM enrollments WHERE customer_id = ?",
                student.customerId().toString())).isEqualTo(1);
    }

    @Test
    @DisplayName("retorna 400 ao matricular com lista vazia")
    void shouldRejectEmptyList() {
        as(newStudent()).body(Map.of("activityClassIds", List.of())).when().post("/me/enrollment")
                .then().statusCode(400)
                .body("errors.activityClassIds", not(emptyOrNullString()));
    }

    @Test
    @DisplayName("retorna 400 quando a mesma atividade é escolhida duas vezes")
    void shouldRejectDuplicatedActivity() {
        as(newStudent()).body(enrollIn(SeedData.SWIMMING_CLASS, SeedData.SWIMMING_CLASS)).when().post("/me/enrollment")
                .then().statusCode(400)
                .body("message", equalTo("The same activity cannot be selected more than once."));
    }

    @Test
    @DisplayName("retorna 404 ao matricular em atividade desconhecida")
    void shouldRejectUnknownActivity() {
        UUID unknown = UUID.randomUUID();

        as(newStudent()).body(enrollIn(unknown)).when().post("/me/enrollment")
                .then().statusCode(404)
                .body("message", equalTo("Activity class not found: " + unknown));
    }

    @Test
    @DisplayName("retorna 409 ao matricular em atividade inativa")
    void shouldRejectInactiveActivity() {
        UUID inactive = DbFixtures.insertActivity(SeedData.SPORT_JUDO, SeedData.ROOM_JUDO, SeedData.TRAINER_CARLOS,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 10, new BigDecimal("90"), false, DayOfWeek.FRIDAY);

        as(newStudent()).body(enrollIn(inactive)).when().post("/me/enrollment")
                .then().statusCode(409)
                .body("message", equalTo("The activity is inactive: " + inactive));
    }

    @Test
    @DisplayName("retorna 409 quando o aluno já está matriculado na atividade")
    void shouldRejectRepeatedEnrollment() {
        Student student = newStudent();
        enrollInSwimming(student);

        as(student).body(enrollIn(SeedData.SWIMMING_CLASS)).when().post("/me/enrollment")
                .then().statusCode(409)
                .body("message", equalTo("The customer is already enrolled in this activity"));
    }

    @Test
    @DisplayName("retorna 409 quando a atividade não tem vagas")
    void shouldRejectFullActivity() {
        UUID gym = gymOnMonday(LocalTime.of(8, 0), LocalTime.of(9, 0), 1);
        DbFixtures.fillActivity(gym, new BigDecimal("120"), 1);

        as(newStudent()).body(enrollIn(gym)).when().post("/me/enrollment")
                .then().statusCode(409)
                .body("message", equalTo("The activity has no available vacancies: " + gym));
    }

    @Test
    @DisplayName("retorna 409 quando as atividades escolhidas têm conflito de horário")
    void shouldRejectConflictingSelection() {
        UUID gym = gymOnMonday(LocalTime.of(12, 0), LocalTime.of(13, 0), 10);

        as(newStudent()).body(enrollIn(SeedData.SWIMMING_CLASS, gym)).when().post("/me/enrollment")
                .then().statusCode(409)
                .body("message", equalTo("The selected activities have conflicting schedules"));
    }

    @Test
    @DisplayName("retorna 409 quando a atividade conflita com outra que o aluno já faz")
    void shouldRejectConflictWithCurrentActivity() {
        Student student = newStudent();
        enrollInSwimming(student);
        UUID gym = gymOnMonday(LocalTime.of(12, 0), LocalTime.of(13, 0), 10);

        as(student).body(enrollIn(gym)).when().post("/me/enrollment")
                .then().statusCode(409)
                .body("message", equalTo("The activity conflicts with the customer's schedule"));
    }

    @Test
    @DisplayName("retorna 403 para o administrador ao matricular")
    void shouldForbidAdminToEnroll() {
        asAdmin().body(enrollIn(SeedData.SWIMMING_CLASS)).when().post("/me/enrollment").then().statusCode(403);
    }

    @Test
    @DisplayName("retorna 401 ao matricular sem token")
    void shouldRequireTokenToEnroll() {
        anonymous().body(enrollIn(SeedData.SWIMMING_CLASS)).when().post("/me/enrollment").then().statusCode(401);
    }

    @Test
    @DisplayName("retorna a matrícula do aluno autenticado")
    void shouldReturnEnrollment() {
        Student student = newStudent();
        String enrollmentActivityId = enrollInSwimming(student);

        as(student).when().get("/me/enrollment")
                .then().statusCode(200)
                .body("customerId", equalTo(student.customerId().toString()))
                .body("activities.enrollmentActivityId", contains(enrollmentActivityId));
    }

    @Test
    @DisplayName("não mostra a matrícula de outro aluno")
    void shouldIsolateStudents() {
        enrollInSwimming(newStudent());

        as(newStudent()).when().get("/me/enrollment").then().statusCode(404);
    }

    @Test
    @DisplayName("retorna 404 quando o aluno não tem matrícula")
    void shouldReturnNotFoundWithoutEnrollment() {
        as(newStudent()).when().get("/me/enrollment")
                .then().statusCode(404)
                .contentType(ContentType.JSON)
                .body("message", equalTo("No enrollment was found for the authenticated student."));
    }

    @Test
    @DisplayName("retorna 403 para o administrador ao consultar matrícula")
    void shouldForbidAdminToFindEnrollment() {
        asAdmin().when().get("/me/enrollment").then().statusCode(403);
    }

    @Test
    @DisplayName("retorna 401 ao consultar matrícula sem token")
    void shouldRequireTokenToFindEnrollment() {
        anonymous().when().get("/me/enrollment").then().statusCode(401);
    }

    @Test
    @DisplayName("cancela a atividade com a data informada e desativa a matrícula")
    void shouldCancelActivity() {
        Student student = newStudent();
        String enrollmentActivityId = enrollInSwimming(student);
        LocalDate endDate = LocalDate.now().plusDays(10);

        as(student).queryParam("endDate", endDate.toString())
                .when().delete("/me/enrollment/activities/{id}", enrollmentActivityId)
                .then().statusCode(200)
                .body("active", is(false))
                .body("activities[0].active", is(false))
                .body("activities[0].endDate", equalTo(endDate.toString()));
    }

    @Test
    @DisplayName("usa a data atual quando endDate não é informado")
    void shouldUseTodayByDefault() {
        Student student = newStudent();
        String enrollmentActivityId = enrollInSwimming(student);

        as(student).when().delete("/me/enrollment/activities/{id}", enrollmentActivityId)
                .then().statusCode(200)
                .body("activities[0].endDate", equalTo(LocalDate.now().toString()));
    }

    @Test
    @DisplayName("mantém a matrícula ativa enquanto houver outra atividade ativa")
    void shouldKeepEnrollmentActiveWithOtherActivities() {
        Student student = newStudent();
        UUID gym = gymOnMonday(LocalTime.of(8, 0), LocalTime.of(9, 0), 10);
        String swimmingEnrollment = as(student).body(enrollIn(SeedData.SWIMMING_CLASS, gym)).when().post("/me/enrollment")
                .then().statusCode(201)
                .extract().path("activities.find { it.activityClassId == '%s' }.enrollmentActivityId"
                        .formatted(SeedData.SWIMMING_CLASS));

        as(student).when().delete("/me/enrollment/activities/{id}", swimmingEnrollment)
                .then().statusCode(200)
                .body("active", is(true))
                .body("activities.findAll { it.active }.activityClassId", contains(gym.toString()));
    }

    @Test
    @DisplayName("libera a vaga da atividade cancelada")
    void shouldFreeSpot() {
        Student student = newStudent();
        String enrollmentActivityId = enrollInSwimming(student);

        as(student).when().delete("/me/enrollment/activities/{id}", enrollmentActivityId).then().statusCode(200);

        asAdmin().when().get("/activity-classes")
                .then().body("[0].availableSpots", equalTo(SeedData.SWIMMING_CAPACITY));
    }

    @Test
    @DisplayName("permite nova matrícula na atividade depois de cancelar")
    void shouldAllowReenrollmentAfterCancel() {
        Student student = newStudent();
        String enrollmentActivityId = enrollInSwimming(student);
        as(student).when().delete("/me/enrollment/activities/{id}", enrollmentActivityId).then().statusCode(200);

        as(student).body(enrollIn(SeedData.SWIMMING_CLASS)).when().post("/me/enrollment")
                .then().statusCode(201)
                .body("active", is(true))
                .body("activities", hasSize(2));
    }

    @Test
    @DisplayName("retorna 409 ao cancelar atividade já inativa")
    void shouldRejectAlreadyCanceledActivity() {
        Student student = newStudent();
        String enrollmentActivityId = enrollInSwimming(student);
        as(student).when().delete("/me/enrollment/activities/{id}", enrollmentActivityId).then().statusCode(200);

        as(student).when().delete("/me/enrollment/activities/{id}", enrollmentActivityId)
                .then().statusCode(409)
                .body("message", equalTo("Enrollment activity is already inactive"));
    }

    @Test
    @DisplayName("retorna 409, como documentado, para cancelamento antes da data de início")
    void shouldRejectDateBeforeStart() {
        Student student = newStudent();
        String enrollmentActivityId = enrollInSwimming(student);

        as(student).queryParam("endDate", LocalDate.now().minusDays(1).toString())
                .when().delete("/me/enrollment/activities/{id}", enrollmentActivityId)
                .then().statusCode(409);
    }

    @Test
    @DisplayName("retorna 404 ao cancelar atividade que não está na matrícula do aluno")
    void shouldRejectUnknownEnrollmentActivity() {
        Student student = newStudent();
        enrollInSwimming(student);

        as(student).when().delete("/me/enrollment/activities/{id}", UUID.randomUUID())
                .then().statusCode(404)
                .body("message", equalTo("Enrollment activity not found"));
    }

    @Test
    @DisplayName("não deixa um aluno cancelar a atividade de outro")
    void shouldNotCancelActivityOfAnotherStudent() {
        String othersActivity = enrollInSwimming(newStudent());
        Student intruder = newStudent();
        enrollInSwimming(intruder);

        as(intruder).when().delete("/me/enrollment/activities/{id}", othersActivity).then().statusCode(404);
        assertThat(DbFixtures.text("SELECT status FROM enrollment_activities WHERE id = ?", othersActivity)).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("retorna 404 ao cancelar quando o aluno não tem matrícula")
    void shouldRejectWithoutEnrollment() {
        as(newStudent()).when().delete("/me/enrollment/activities/{id}", UUID.randomUUID())
                .then().statusCode(404)
                .body("message", equalTo("Enrollment not found"));
    }

    @Test
    @DisplayName("retorna 403 para o administrador ao cancelar")
    void shouldForbidAdminToCancel() {
        asAdmin().when().delete("/me/enrollment/activities/{id}", UUID.randomUUID()).then().statusCode(403);
    }

    @Test
    @DisplayName("retorna 401 ao cancelar sem token")
    void shouldRequireTokenToCancel() {
        anonymous().when().delete("/me/enrollment/activities/{id}", UUID.randomUUID()).then().statusCode(401);
    }
}
