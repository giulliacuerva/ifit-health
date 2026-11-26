package br.ifsp.demo.integration.api;

import br.ifsp.demo.support.ApiTestBase;
import br.ifsp.demo.support.DbFixtures;
import br.ifsp.demo.support.SeedData;
import io.restassured.response.ValidatableResponse;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;

@Tag("ApiTest")
@Tag("IntegrationTest")
@DisplayName("API de relatórios: GET /activity-reports")
class ActivityReportApiTest extends ApiTestBase {
    private static final LocalDate MARCH_1 = LocalDate.of(2026, 3, 1);
    private static final LocalDate MARCH_31 = LocalDate.of(2026, 3, 31);
    private static final String SWIMMING_ITEM = "items.find { it.activityClassId == '%s' }".formatted(SeedData.SWIMMING_CLASS);

    private ValidatableResponse report(LocalDate start, LocalDate end, String... extraParams) {
        var request = asAdmin().queryParam("startDate", start.toString()).queryParam("endDate", end.toString());
        for (int i = 0; i < extraParams.length; i += 2) {
            request = request.queryParam(extraParams[i], extraParams[i + 1]);
        }
        return request.get("/activity-reports").then();
    }

    private UUID judoOnTuesday() {
        return DbFixtures.insertActivity(SeedData.SPORT_JUDO, SeedData.ROOM_JUDO, SeedData.TRAINER_JOHN,
                LocalTime.of(18, 0), LocalTime.of(19, 0), 10, new BigDecimal("100"), true, DayOfWeek.TUESDAY);
    }

    @Test
    @DisplayName("retorna o período e todas as atividades zeradas quando não há matrículas")
    void shouldReturnEmptyValuesWithoutEnrollments() {
        report(MARCH_1, MARCH_31)
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("startDate", equalTo(MARCH_1.toString()))
                .body("endDate", equalTo(MARCH_31.toString()))
                .body("items.size()", equalTo(1))
                .body(SWIMMING_ITEM + ".capacity", equalTo(SeedData.SWIMMING_CAPACITY))
                .body(SWIMMING_ITEM + ".enrollmentCount", equalTo(0))
                .body(SWIMMING_ITEM + ".revenue", isNumber(0))
                .body(SWIMMING_ITEM + ".averageOccupancyPercent", isNumber(0));
    }

    @Test
    @DisplayName("conta matrículas, soma receita e calcula ocupação no período de um mês")
    void shouldCalculateValuesForEnrollmentsInThePeriod() {
        DbFixtures.enrollNewCustomer(SeedData.SWIMMING_CLASS, SeedData.SWIMMING_FEE, MARCH_1);
        DbFixtures.enrollNewCustomer(SeedData.SWIMMING_CLASS, SeedData.SWIMMING_FEE, MARCH_1);

        report(MARCH_1, MARCH_31)
                .statusCode(200)
                .body(SWIMMING_ITEM + ".enrollmentCount", equalTo(2))
                .body(SWIMMING_ITEM + ".revenue", isNumber(360))
                .body(SWIMMING_ITEM + ".averageOccupancyPercent", isNumber(10.0));
    }

    @Test
    @DisplayName("não conta matrícula encerrada antes do período do relatório")
    void shouldIgnoreEnrollmentEndedBeforePeriod() {
        UUID enrollment = DbFixtures.insertEnrollment(DbFixtures.insertCustomer(), true);
        DbFixtures.insertEnrollmentActivity(enrollment, SeedData.SWIMMING_CLASS, SeedData.SWIMMING_FEE,
                LocalDate.of(2026, 1, 5), LocalDate.of(2026, 2, 10), false);

        report(MARCH_1, MARCH_31)
                .statusCode(200)
                .body(SWIMMING_ITEM + ".enrollmentCount", equalTo(0))
                .body(SWIMMING_ITEM + ".revenue", isNumber(0));
    }

    @Test
    @DisplayName("cobra todos os meses em que a matrícula ficou ativa")
    void shouldChargeEveryActiveMonth() {
        DbFixtures.enrollNewCustomer(SeedData.SWIMMING_CLASS, SeedData.SWIMMING_FEE, LocalDate.of(2026, 1, 1));

        report(LocalDate.of(2026, 1, 1), MARCH_31)
                .statusCode(200)
                .body(SWIMMING_ITEM + ".revenue", isNumber(540));
    }

    @Test
    @DisplayName("conta o aluno uma vez só mesmo em período de vários meses")
    void shouldCountEachStudentOnce() {
        DbFixtures.enrollNewCustomer(SeedData.SWIMMING_CLASS, SeedData.SWIMMING_FEE, LocalDate.of(2026, 1, 1));

        report(LocalDate.of(2026, 1, 1), MARCH_31)
                .statusCode(200)
                .body(SWIMMING_ITEM + ".enrollmentCount", equalTo(1));
    }

    @Test
    @DisplayName("retorna só as atividades da modalidade filtrada no relatório")
    void shouldFilterBySport() {
        UUID judo = judoOnTuesday();
        DbFixtures.enrollNewCustomer(judo, new BigDecimal("100"), MARCH_1);
        DbFixtures.enrollNewCustomer(SeedData.SWIMMING_CLASS, SeedData.SWIMMING_FEE, MARCH_1);

        report(MARCH_1, MARCH_31, "sportId", SeedData.SPORT_JUDO.toString())
                .statusCode(200)
                .body("items.size()", equalTo(1))
                .body("items[0].activityClassId", equalTo(judo.toString()))
                .body("items[0].enrollmentCount", equalTo(1))
                .body("items[0].revenue", isNumber(100));
    }

    @Test
    @DisplayName("retorna só as atividades do dia da semana filtrado no relatório")
    void shouldFilterByWeekday() {
        UUID judo = judoOnTuesday();
        DbFixtures.enrollNewCustomer(judo, new BigDecimal("100"), MARCH_1);
        DbFixtures.enrollNewCustomer(SeedData.SWIMMING_CLASS, SeedData.SWIMMING_FEE, MARCH_1);

        report(MARCH_1, MARCH_31, "dayOfWeek", "MONDAY")
                .statusCode(200)
                .body("items.size()", equalTo(1))
                .body("items[0].activityClassId", equalTo(SeedData.SWIMMING_CLASS.toString()));
    }

    @Test
    @DisplayName("retorna lista vazia quando modalidade e dia da semana não combinam")
    void shouldReturnEmptyWhenSportAndWeekdayDoNotMatch() {
        DbFixtures.enrollNewCustomer(SeedData.SWIMMING_CLASS, SeedData.SWIMMING_FEE, MARCH_1);

        report(MARCH_1, MARCH_31, "sportId", SeedData.SPORT_SWIMMING.toString(), "dayOfWeek", "FRIDAY")
                .statusCode(200)
                .body("items", empty());
    }

    @Test
    @DisplayName("mantém os mesmos valores com e sem filtro para matrícula anterior ao período")
    void shouldKeepValuesConsistentWhenFilteringBySport() {
        DbFixtures.enrollNewCustomer(SeedData.SWIMMING_CLASS, SeedData.SWIMMING_FEE, LocalDate.of(2026, 2, 10));

        report(MARCH_1, MARCH_31).statusCode(200)
                .body(SWIMMING_ITEM + ".revenue", isNumber(180));

        report(MARCH_1, MARCH_31, "sportId", SeedData.SPORT_SWIMMING.toString())
                .statusCode(200)
                .body("items.size()", equalTo(1))
                .body(SWIMMING_ITEM + ".revenue", isNumber(180));
    }

    @Test
    @DisplayName("calcula a ocupação média quando há filtro no relatório")
    void shouldCalculateOccupancyWithFilter() {
        DbFixtures.enrollNewCustomer(SeedData.SWIMMING_CLASS, SeedData.SWIMMING_FEE, MARCH_1);
        DbFixtures.enrollNewCustomer(SeedData.SWIMMING_CLASS, SeedData.SWIMMING_FEE, MARCH_1);

        report(MARCH_1, MARCH_31, "dayOfWeek", "MONDAY")
                .statusCode(200)
                .body(SWIMMING_ITEM + ".averageOccupancyPercent", isNumber(10.0));
    }

    @Test
    @DisplayName("aceita data inicial igual à final no relatório")
    void shouldAcceptSingleDayPeriod() {
        report(MARCH_1, MARCH_1).statusCode(200).body("startDate", equalTo(MARCH_1.toString()));
    }

    @Test
    @DisplayName("retorna 400 quando a data final é anterior à inicial no relatório")
    void shouldRejectEndBeforeStart() {
        report(MARCH_31, MARCH_1)
                .statusCode(400)
                .body("message", equalTo("End date cannot be before start date"));
    }

    @Test
    @DisplayName("retorna 404 para modalidade desconhecida no relatório")
    void shouldRejectUnknownSport() {
        UUID unknown = UUID.randomUUID();

        report(MARCH_1, MARCH_31, "sportId", unknown.toString())
                .statusCode(404)
                .body("message", equalTo("Sport not found: " + unknown));
    }

    @Test
    @DisplayName("retorna 400 quando falta a data inicial obrigatória no relatório")
    void shouldRejectMissingStartDate() {
        asAdmin().queryParam("endDate", MARCH_31.toString()).when().get("/activity-reports").then().statusCode(400);
    }

    @Test
    @DisplayName("retorna 400 para dia da semana inválido no relatório")
    void shouldRejectInvalidWeekday() {
        report(MARCH_1, MARCH_31, "dayOfWeek", "SEGUNDA").statusCode(400);
    }

    @Test
    @DisplayName("retorna 403 para aluno no relatório")
    void shouldForbidStudents() {
        as(newStudent()).queryParam("startDate", MARCH_1.toString()).queryParam("endDate", MARCH_31.toString())
                .when().get("/activity-reports").then().statusCode(403);
    }

    @Test
    @DisplayName("retorna 401 no relatório sem token")
    void shouldRequireAuthentication() {
        anonymous().queryParam("startDate", MARCH_1.toString()).queryParam("endDate", MARCH_31.toString())
                .when().get("/activity-reports").then().statusCode(401);
    }
}
