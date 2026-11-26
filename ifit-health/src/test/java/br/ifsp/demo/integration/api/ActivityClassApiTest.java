package br.ifsp.demo.integration.api;

import br.ifsp.demo.support.ApiTestBase;
import br.ifsp.demo.support.DbFixtures;
import br.ifsp.demo.support.FakeData;
import br.ifsp.demo.support.SeedData;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@Tag("ApiTest")
@Tag("IntegrationTest")
@DisplayName("API de turmas: /activity-classes")
class ActivityClassApiTest extends ApiTestBase {
    @Test
    @DisplayName("retorna as atividades com nomes do catálogo e disponibilidade")
    void shouldListActivitiesWithAvailability() {
        DbFixtures.fillActivity(SeedData.SWIMMING_CLASS, SeedData.SWIMMING_FEE, 3);

        asAdmin().when().get("/activity-classes")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(1))
                .body("[0].id", equalTo(SeedData.SWIMMING_CLASS.toString()))
                .body("[0].sportName", equalTo(SeedData.SWIMMING_NAME))
                .body("[0].roomName", equalTo("Piscina"))
                .body("[0].trainerName", equalTo(SeedData.TRAINER_JULIANA_NAME))
                .body("[0].weekdays", contains("MONDAY"))
                .body("[0].startTime", equalTo("11:30:00"))
                .body("[0].endTime", equalTo("12:30:00"))
                .body("[0].capacity", equalTo(SeedData.SWIMMING_CAPACITY))
                .body("[0].enrolledCount", equalTo(3))
                .body("[0].availableSpots", equalTo(SeedData.SWIMMING_CAPACITY - 3))
                .body("[0].active", is(true));
    }

    @Test
    @DisplayName("filtra as atividades pelo parâmetro active")
    void shouldFilterByActive() {
        UUID inactive = DbFixtures.insertActivity(SeedData.SPORT_JUDO, SeedData.ROOM_JUDO, SeedData.TRAINER_CARLOS,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 10, new BigDecimal("90"), false, DayOfWeek.FRIDAY);

        asAdmin().queryParam("active", false).when().get("/activity-classes")
                .then().statusCode(200)
                .body("id", contains(inactive.toString()))
                .body("[0].availableSpots", equalTo(0));

        asAdmin().queryParam("active", true).when().get("/activity-classes")
                .then().statusCode(200)
                .body("id", contains(SeedData.SWIMMING_CLASS.toString()));
    }

    @Test
    @DisplayName("libera a listagem de atividades para alunos")
    void shouldListActivitiesForStudents() {
        as(newStudent()).when().get("/activity-classes")
                .then().statusCode(200)
                .body("id", hasItem(SeedData.SWIMMING_CLASS.toString()));
    }

    @Test
    @DisplayName("retorna 401 ao listar atividades sem token")
    void shouldRequireTokenToListActivities() {
        anonymous().when().get("/activity-classes").then().statusCode(401);
    }

    @Test
    @DisplayName("retorna os dados da atividade buscada por id")
    void shouldReturnActivity() {
        asAdmin().when().get("/activity-classes/{id}", SeedData.SWIMMING_CLASS).then().statusCode(200).contentType(ContentType.JSON)
            .body("id", equalTo(SeedData.SWIMMING_CLASS.toString()))
            .body("sportId", equalTo(SeedData.SPORT_SWIMMING.toString()))
            .body("roomId", equalTo(SeedData.ROOM_POOL.toString()))
            .body("trainerId", equalTo(SeedData.TRAINER_JULIANA.toString()))
            .body("weekdays", contains(SeedData.SWIMMING_DAY.name()))
            .body("startTime", equalTo("11:30:00"))
            .body("endTime", equalTo("12:30:00"))
            .body("capacity", equalTo(SeedData.SWIMMING_CAPACITY))
            .body("monthlyFee", isNumber(SeedData.SWIMMING_FEE.doubleValue()))
            .body("active", is(true));
    }

    @Test
    @DisplayName("retorna 404 ao buscar atividade com id desconhecido")
    void shouldReturnNotFoundForUnknownActivity() {
        UUID unknown = UUID.randomUUID();

        asAdmin().when().get("/activity-classes/{id}", unknown).then().statusCode(404)
            .body("message", equalTo("Activity class not found: " + unknown));
    }

    @Test
    @DisplayName("retorna 400 ao buscar atividade com id que não é UUID")
    void shouldRejectInvalidId() {
        asAdmin().when().get("/activity-classes/{id}", "not-a-uuid").then().statusCode(400);
    }

    @Test
    @DisplayName("retorna 401 ao buscar atividade sem token")
    void shouldRequireTokenToFindActivity() {
        anonymous().when().get("/activity-classes/{id}", SeedData.SWIMMING_CLASS).then().statusCode(401);
    }

    @Test
    @DisplayName("retorna 201 com a atividade criada e grava no banco")
    void shouldCreateActivity() {
        Map<String, Object> body = validRequest();

        String id = asAdmin().body(body).when().post("/activity-classes").then().statusCode(201).contentType(ContentType.JSON)
            .body("id", notNullValue())
            .body("sportId", equalTo(body.get("sportId")))
            .body("roomId", equalTo(body.get("roomId")))
            .body("trainerId", equalTo(body.get("trainerId")))
            .body("weekdays", containsInAnyOrder("TUESDAY", "THURSDAY"))
            .body("startTime", equalTo(body.get("startTime")))
            .body("endTime", equalTo(body.get("endTime")))
            .body("capacity", equalTo(body.get("capacity")))
            .body("monthlyFee", isNumber(((Number) body.get("monthlyFee")).doubleValue()))
            .body("active", is(true))
            .extract()
            .path("id");

        asAdmin().when().get("/activity-classes/{id}", id).then().statusCode(200)
            .body("sportId", equalTo(body.get("sportId")))
            .body("roomId", equalTo(body.get("roomId")))
            .body("trainerId", equalTo(body.get("trainerId")))
            .body("weekdays", containsInAnyOrder("TUESDAY", "THURSDAY"))
            .body("startTime", equalTo(body.get("startTime")))
            .body("endTime", equalTo(body.get("endTime")))
            .body("capacity", equalTo(body.get("capacity")))
            .body("monthlyFee", isNumber(((Number) body.get("monthlyFee")).doubleValue()))
            .body("active", is(true));

        assertThat(DbFixtures.count("SELECT COUNT(*) FROM activity_class WHERE id = ?", id)).isEqualTo(1);
        assertThat(DbFixtures.count("SELECT COUNT(*) FROM activity_prices WHERE activity_id = ?", id)).isEqualTo(1);
    }

    private Map<String, Object> validRequest() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sportId", SeedData.SPORT_SWIMMING.toString());
        body.put("roomId", SeedData.ROOM_POOL.toString());
        body.put("trainerId", SeedData.TRAINER_JULIANA.toString());
        body.put("weekdays", List.of(DayOfWeek.TUESDAY.name(), DayOfWeek.THURSDAY.name()));
        body.put("startTime", "08:00:00");
        body.put("endTime", "09:00:00");
        body.put("capacity", 12);
        body.put("monthlyFee", 150.00);
        return body;
    }

    static Stream<Arguments> invalidFields() {
        return Stream.of(
            Arguments.of("sportId", null),
            Arguments.of("roomId", null),
            Arguments.of("trainerId", null),
            Arguments.of("weekdays", List.of()),
            Arguments.of("startTime", null),
            Arguments.of("endTime", null),
            Arguments.of("capacity", 0),
            Arguments.of("monthlyFee", null),
            Arguments.of("monthlyFee", new BigDecimal("-1.00")));
    }

    @ParameterizedTest(name = "[{index}] {0} = {1}")
    @MethodSource("invalidFields")
    @DisplayName("retorna 400 e identifica o campo inválido ao criar atividade")
    void shouldRejectInvalidFieldOnCreate(String field, Object value) {
        Map<String, Object> body = validRequest();
        body.put(field, value);

        asAdmin().body(body).when().post("/activity-classes").then().statusCode(400)
            .body("errors." + field, not(emptyOrNullString()));
    }

    @Test
    @DisplayName("retorna 400 quando o horário final não é depois do inicial")
    void shouldRejectEndBeforeStart() {
        Map<String, Object> body = validRequest();
        body.put("startTime", "10:00:00");
        body.put("endTime", "09:00:00");

        asAdmin().body(body).when().post("/activity-classes").then().statusCode(400)
            .body("message", equalTo("End time must be after start time"));
    }

    @ParameterizedTest(name = "[{index}] {0}-{1}")
    @CsvSource({
        "05:00:00, 06:30:00",
        "21:30:00, 22:01:00"
    })
    @DisplayName("retorna 400 para horário fora de 06:00-22:00")
    void shouldRejectScheduleOutsideOpeningHours(String startTime, String endTime) {
        Map<String, Object> body = validRequest();
        body.put("startTime", startTime);
        body.put("endTime", endTime);

        asAdmin().body(body).when().post("/activity-classes").then().statusCode(400)
            .body("message", equalTo("Schedule must be between 06:00 and 22:00"));
    }

    @ParameterizedTest(name = "[{index}] {0}-{1}")
    @CsvSource({
        "06:00:00, 06:30:00",
        "21:30:00, 22:00:00"
    })
    @DisplayName("aceita os limites 06:00 e 22:00 ao criar atividade")
    void shouldAcceptOpeningHourLimits(String startTime, String endTime) {
        Map<String, Object> body = validRequest();
        body.put("startTime", startTime);
        body.put("endTime", endTime);

        asAdmin().body(body).when().post("/activity-classes").then().statusCode(201);
    }

    @ParameterizedTest(name = "[{index}] referencia desconhecida: {0}")
    @CsvSource({
        "roomId, Room",
        "sportId, Sport",
        "trainerId, Trainer"
    })
    @DisplayName("retorna 404 para sala, modalidade ou professor desconhecido")
    void shouldRejectUnknownReferences(String field, String referenceName) {
        UUID unknown = UUID.randomUUID();
        Map<String, Object> body = validRequest();
        body.put(field, unknown.toString());

        asAdmin().body(body).when().post("/activity-classes").then().statusCode(404)
            .body("message", equalTo(referenceName + " not found: " + unknown));
    }

    @Test
    @DisplayName("retorna 409 quando o tipo de sala não serve para a modalidade")
    void shouldRejectRoomTypeMismatch() {
        Map<String, Object> body = validRequest();
        body.put("roomId", SeedData.ROOM_GYM.toString());

        asAdmin().body(body).when().post("/activity-classes").then().statusCode(409)
            .body("message", equalTo("Room type does not match sport requirements"));
    }

    @Test
    @DisplayName("retorna 409 quando a capacidade passa a da sala")
    void shouldRejectCapacityAboveRoomLimit() {
        Map<String, Object> body = validRequest();
        body.put("roomId", SeedData.ROOM_POOL.toString());
        body.put("capacity", SeedData.ROOM_POOL_CAPACITY + 1);

        asAdmin().body(body).when().post("/activity-classes").then().statusCode(409)
            .body("message", equalTo("Capacity exceeds room limit"));
    }

    @Test
    @DisplayName("retorna 409 quando a sala já está ocupada no horário")
    void shouldRejectRoomConflict() {
        Map<String, Object> body = validRequest();
        body.put("weekdays", List.of(DayOfWeek.MONDAY.name()));
        body.put("startTime", "12:00:00");
        body.put("endTime", "13:00:00");

        asAdmin().body(body).when().post("/activity-classes")
                .then().statusCode(409)
                .body("message", equalTo("Room is already booked for the given schedule"));
    }

    @Test
    @DisplayName("retorna 409 quando o professor já está ocupado no horário")
    void shouldRejectTrainerConflict() {
        Map<String, Object> body = validRequest();
        body.put("roomId", SeedData.ROOM_GYM.toString());
        body.put("weekdays", List.of(DayOfWeek.MONDAY.name()));
        body.put("startTime", "12:00:00");
        body.put("endTime", "13:00:00");

        asAdmin().body(body).when().post("/activity-classes")
            .then().statusCode(409)
            .body("message", equalTo("Trainer is already booked for the given schedule"));
    }

    @Test
    @DisplayName("permite aulas seguidas na mesma sala")
    void shouldAllowAdjacentSchedules() {
        Map<String, Object> body = validRequest();
        body.put("trainerId", SeedData.TRAINER_MARIA.toString());
        body.put("weekdays", List.of(DayOfWeek.MONDAY.name()));
        body.put("startTime", "12:30:00");
        body.put("endTime", "13:30:00");

        asAdmin().body(body).when().post("/activity-classes").then().statusCode(201);
    }

    @Test
    @DisplayName("retorna 403 para aluno ao criar atividade")
    void shouldForbidStudentsToCreate() {
        int countBefore = DbFixtures.count("SELECT COUNT(*) FROM activity_class");

        as(newStudent()).body(validRequest()).when().post("/activity-classes")
            .then().statusCode(403);

        assertThat(DbFixtures.count("SELECT COUNT(*) FROM activity_class")).isEqualTo(countBefore);
    }

    @Test
    @DisplayName("retorna 401 ao criar atividade sem token")
    void shouldRequireTokenToCreateActivity() {
        anonymous().body(validRequest()).when().post("/activity-classes").then().statusCode(401);
    }

    @Test
    @DisplayName("retorna 200 com o novo horário e a nova capacidade ao editar")
    void shouldEditActivity() {
        Map<String, Object> body = validRequest();
        body.put("weekdays", List.of(DayOfWeek.MONDAY.name(), DayOfWeek.WEDNESDAY.name()));
        body.put("startTime", "14:00:00");
        body.put("endTime", "15:30:00");
        body.put("capacity", 15);

        asAdmin().body(body).when().put("/activity-classes/{id}", SeedData.SWIMMING_CLASS).then().statusCode(200)
            .body("id", equalTo(SeedData.SWIMMING_CLASS.toString()))
            .body("weekdays", containsInAnyOrder("MONDAY", "WEDNESDAY"))
            .body("startTime", equalTo("14:00:00"))
            .body("endTime", equalTo("15:30:00"))
            .body("capacity", equalTo(15));

        asAdmin().when().get("/activity-classes/{id}", SeedData.SWIMMING_CLASS).then()
            .body("capacity", equalTo(15))
            .body("startTime", equalTo("14:00:00"));
    }

    @Test
    @DisplayName("agenda nova mensalidade para o mês seguinte sem mudar o valor atual dos alunos")
    void shouldScheduleNewFeeForNextMonth() {
        Map<String, Object> body = validRequest();
        body.put("weekdays", List.of(SeedData.SWIMMING_DAY.name()));
        body.put("startTime", SeedData.SWIMMING_START.toString());
        body.put("endTime", SeedData.SWIMMING_END.toString());
        body.put("capacity", SeedData.SWIMMING_CAPACITY);
        body.put("monthlyFee", 210);

        asAdmin().body(body).when().put("/activity-classes/{id}", SeedData.SWIMMING_CLASS)
            .then().statusCode(200)
            .body("monthlyFee", isNumber(210));

        asAdmin().when().get("/activity-classes/{id}", SeedData.SWIMMING_CLASS).then()
            .body("monthlyFee", isNumber(210));
        as(newStudent()).when().get("/activity-classes/{id}", SeedData.SWIMMING_CLASS).then()
            .body("monthlyFee", isNumber(180));

        String validFrom = DbFixtures.text("SELECT valid_from FROM activity_prices WHERE activity_id = ? AND monthly_fee = 210",
                SeedData.SWIMMING_CLASS.toString());
        assertThat(validFrom).isEqualTo(LocalDate.now().withDayOfMonth(1).plusMonths(1).toString());
    }

    @Test
    @DisplayName("retorna 409 quando a nova capacidade é menor que os matriculados")
    void shouldRejectCapacityBelowEnrolledStudents() {
        DbFixtures.fillActivity(SeedData.SWIMMING_CLASS, SeedData.SWIMMING_FEE, 2);

        Map<String, Object> body = validRequest();
        body.put("weekdays", List.of(SeedData.SWIMMING_DAY.name()));
        body.put("startTime", SeedData.SWIMMING_START.toString());
        body.put("endTime", SeedData.SWIMMING_END.toString());
        body.put("capacity", 1);
        body.put("monthlyFee", SeedData.SWIMMING_FEE);

        asAdmin().body(body).when().put("/activity-classes/{id}", SeedData.SWIMMING_CLASS).then().statusCode(409)
            .body("message", equalTo("New capacity cannot be lower than enrolled students: 2"));
    }

    @Test
    @DisplayName("retorna 409 listando os alunos afetados por conflito de horário")
    void shouldReturnAffectedStudentsOnConflict() {
        UUID gymClass = DbFixtures.insertActivity(SeedData.SPORT_WEIGHT_TRAINING, SeedData.ROOM_GYM, SeedData.TRAINER_JOHN,
                LocalTime.of(8, 0), LocalTime.of(9, 0), 10, new BigDecimal("120"), true, DayOfWeek.WEDNESDAY);
        String studentName = FakeData.firstName() + " " + FakeData.lastName();
        UUID customer = DbFixtures.insertCustomer(studentName, FakeData.cpf(), FakeData.email());
        UUID enrollment = DbFixtures.insertEnrollment(customer, true);
        DbFixtures.insertEnrollmentActivity(enrollment, SeedData.SWIMMING_CLASS, SeedData.SWIMMING_FEE, LocalDate.now(), null, true);
        DbFixtures.insertEnrollmentActivity(enrollment, gymClass, new BigDecimal("120"), LocalDate.now(), null, true);

        Map<String, Object> body = validRequest();
        body.put("sportId", SeedData.SPORT_WEIGHT_TRAINING.toString());
        body.put("roomId", SeedData.ROOM_GYM.toString());
        body.put("trainerId", SeedData.TRAINER_JOHN.toString());
        body.put("weekdays", List.of("MONDAY"));
        body.put("startTime", "12:00");
        body.put("endTime", "13:00");
        body.put("capacity", 10);

        asAdmin().body(body).when().put("/activity-classes/{id}", gymClass).then().statusCode(409).contentType(ContentType.JSON)
            .body("message", equalTo("Student has a schedule conflict"))
            .body("affectedStudents", hasSize(1))
            .body("affectedStudents[0].customerId", equalTo(customer.toString()))
            .body("affectedStudents[0].customerName", equalTo(studentName))
            .body("affectedStudents[0].conflictingActivities[0].activityClassId", equalTo(SeedData.SWIMMING_CLASS.toString()))
            .body("affectedStudents[0].conflictingActivities[0].sportName", equalTo(SeedData.SWIMMING_NAME));
    }

    @Test
    @DisplayName("não acusa o próprio horário como conflito de sala ou professor")
    void shouldIgnoreItsOwnSchedule() {
        Map<String, Object> body = validRequest();
        body.put("weekdays", List.of(SeedData.SWIMMING_DAY.name()));
        body.put("startTime", SeedData.SWIMMING_START.toString());
        body.put("endTime", "13:00:00");
        body.put("capacity", SeedData.SWIMMING_CAPACITY);
        body.put("monthlyFee", SeedData.SWIMMING_FEE);

        asAdmin().body(body).when().put("/activity-classes/{id}", SeedData.SWIMMING_CLASS).then().statusCode(200)
            .body("endTime", equalTo("13:00:00"));
    }

    @ParameterizedTest(name = "[{index}] {0} = {1}")
    @MethodSource("invalidFields")
    @DisplayName("retorna 400 e identifica o campo inválido ao editar atividade")
    void shouldRejectInvalidFieldOnEdit(String field, Object value) {
        Map<String, Object> body = validRequest();
        body.put(field, value);

        asAdmin().body(body).when().put("/activity-classes/{id}", SeedData.SWIMMING_CLASS).then().statusCode(400)
            .body("errors." + field, not(emptyOrNullString()));
    }

    @Test
    @DisplayName("retorna 404 ao editar atividade desconhecida")
    void shouldReturnNotFoundWhenEditingUnknownActivity() {
        UUID unknown = UUID.randomUUID();

        asAdmin().body(validRequest()).when().put("/activity-classes/{id}", unknown).then().statusCode(404)
            .body("message", equalTo("Activity class not found: " + unknown));
    }

    @Test
    @DisplayName("retorna 403 para aluno ao editar atividade")
    void shouldForbidStudentsToEdit() {
        as(newStudent()).body(validRequest()).when().put("/activity-classes/{id}", SeedData.SWIMMING_CLASS)
            .then().statusCode(403);
    }

    @Test
    @DisplayName("retorna 401 ao editar atividade sem token")
    void shouldRequireTokenToEditActivity() {
        anonymous().body(validRequest()).when().put("/activity-classes/{id}", SeedData.SWIMMING_CLASS)
            .then().statusCode(401);
    }
}
