package br.ifsp.demo.integration.persistence;

import br.ifsp.demo.domain.model.ActivityClass;
import br.ifsp.demo.domain.model.Schedule;
import br.ifsp.demo.persistence.sqlite.SqliteActivityClassQueries;
import br.ifsp.demo.persistence.sqlite.SqliteActivityClassQueries.ActivityClassRow;
import br.ifsp.demo.persistence.sqlite.SqliteCatalogQueries;
import br.ifsp.demo.support.DbFixtures;
import br.ifsp.demo.support.PersistenceTestBase;
import br.ifsp.demo.support.SeedData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.UUID;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("PersistenceTest")
@Tag("IntegrationTest")
@DisplayName("Persistência de turmas, histórico de preços e catálogos")
class ActivityClassPersistenceTest extends PersistenceTestBase {
    @Autowired
    private SqliteActivityClassQueries activityQueries;

    @Autowired
    private SqliteCatalogQueries catalogQueries;

    private static final LocalDate NEXT_MONTH = LocalDate.now().withDayOfMonth(1).plusMonths(1);

    private ActivityClass newGymClass(int capacity, String fee) {
        Schedule schedule = new Schedule(EnumSet.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY), LocalTime.of(8, 0), LocalTime.of(9, 0));
        return new ActivityClass(activityClassRepository.findRoomById(SeedData.ROOM_GYM),
            activityClassRepository.findSportById(SeedData.SPORT_WEIGHT_TRAINING),
            activityClassRepository.findTrainerById(SeedData.TRAINER_JOHN), schedule, capacity, new BigDecimal(fee));
    }

    private ActivityClass changeFee(ActivityClass activity, String fee) {
        activity.edit(activity.getRoom(), activity.getSport(), activity.getTrainer(), activity.getSchedule(),
                activity.getCapacity(), new BigDecimal(fee));
        return activityClassRepository.save(activity);
    }

    private ActivityClassRow row(UUID id, boolean latestPrice) {
        return activityQueries.findAll(null, latestPrice).stream().filter(r -> r.id().equals(id)).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("insere a atividade e o primeiro preço válido a partir de hoje")
    void shouldInsertActivityWithInitialPrice() {
        ActivityClass activity = activityClassRepository.save(newGymClass(12, "150.50"));

        assertThat(DbFixtures.text("SELECT weekday FROM activity_class WHERE id = ?", activity.getId().toString()))
                .isEqualTo("TUESDAY,THURSDAY");
        assertThat(DbFixtures.text("SELECT valid_from FROM activity_prices WHERE activity_id = ?", activity.getId().toString()))
                .isEqualTo(LocalDate.now().toString());
    }

    @Test
    @DisplayName("reconstrói a atividade com sala, modalidade, professor e horário")
    void shouldFindActivityById() {
        ActivityClass activity = activity(SeedData.SWIMMING_CLASS);

        assertThat(activity.getSport().getName()).isEqualTo(SeedData.SWIMMING_NAME);
        assertThat(activity.getRoom().getId()).isEqualTo(SeedData.ROOM_POOL);
        assertThat(activity.getTrainer().getName()).isEqualTo(SeedData.TRAINER_JULIANA_NAME);
        assertThat(activity.getSchedule().getWeekdays()).containsExactly(SeedData.SWIMMING_DAY);
        assertThat(activity.getSchedule().getStartTime()).isEqualTo(SeedData.SWIMMING_START);
        assertThat(activity.getSchedule().getEndTime()).isEqualTo(SeedData.SWIMMING_END);
        assertThat(activity.getCapacity()).isEqualTo(SeedData.SWIMMING_CAPACITY);
        assertThat(activity.getMonthlyFee()).isEqualByComparingTo(SeedData.SWIMMING_FEE);
        assertThat(activity.isActive()).isTrue();
    }

    @Test
    @DisplayName("retorna null ao buscar atividade com id desconhecido no repositório")
    void shouldReturnNullForUnknownId() {
        assertThat(activityClassRepository.findById(UUID.randomUUID())).isNull();
    }

    @Test
    @DisplayName("busca por sala e por professor retorna só atividades ativas")
    void shouldFindOnlyActiveActivitiesByRoomAndTrainer() {
        DbFixtures.insertActivity(SeedData.SPORT_SWIMMING, SeedData.ROOM_POOL, SeedData.TRAINER_JULIANA,
            LocalTime.of(15, 0), LocalTime.of(16, 0), 5, new BigDecimal("100"), false, DayOfWeek.FRIDAY);
        var room = activityClassRepository.findRoomById(SeedData.ROOM_POOL);
        var trainer = activityClassRepository.findTrainerById(SeedData.TRAINER_JULIANA);

        assertThat(activityClassRepository.findByRoom(room)).extracting(ActivityClass::getId).containsExactly(SeedData.SWIMMING_CLASS);
        assertThat(activityClassRepository.findByTrainer(trainer)).extracting(ActivityClass::getId).containsExactly(SeedData.SWIMMING_CLASS);
    }

    @Test
    @DisplayName("findAll do repositório retorna atividades ativas e inativas")
    void shouldFindAllActivities() {
        UUID inactive = DbFixtures.insertActivity(SeedData.SPORT_JUDO, SeedData.ROOM_JUDO, SeedData.TRAINER_CARLOS,
            LocalTime.of(9, 0), LocalTime.of(10, 0), 10, new BigDecimal("90"), false, DayOfWeek.FRIDAY);

        assertThat(activityClassRepository.findAll()).extracting(ActivityClass::getId)
            .containsExactlyInAnyOrder(SeedData.SWIMMING_CLASS, inactive);
    }

    @Test
    @DisplayName("mapeia sala, modalidade e professor e retorna null quando não existem")
    void shouldFindCatalogItemsById() {
        assertThat(activityClassRepository.findRoomById(SeedData.ROOM_POOL).getCapacity()).isEqualTo(SeedData.ROOM_POOL_CAPACITY);
        assertThat(activityClassRepository.findSportById(SeedData.SPORT_SWIMMING).getName()).isEqualTo(SeedData.SWIMMING_NAME);
        assertThat(activityClassRepository.findTrainerById(SeedData.TRAINER_JULIANA).getName()).isEqualTo(SeedData.TRAINER_JULIANA_NAME);
        assertThat(activityClassRepository.findRoomById(UUID.randomUUID())).isNull();
        assertThat(activityClassRepository.findSportById(UUID.randomUUID())).isNull();
        assertThat(activityClassRepository.findTrainerById(UUID.randomUUID())).isNull();
    }

    @Test
    @DisplayName("agenda nova mensalidade para o mês seguinte e mantém a vigente")
    void shouldScheduleNewFeeForNextMonth() {
        ActivityClass existing = activity(SeedData.SWIMMING_CLASS);
        existing.edit(
            existing.getRoom(),
            existing.getSport(),
            existing.getTrainer(),
            existing.getSchedule(),
            existing.getCapacity(),
            new BigDecimal("200")
        );

        ActivityClass saved = activityClassRepository.save(existing);
        LocalDate nextMonth = LocalDate.now().withDayOfMonth(1).plusMonths(1);

        assertThat(saved.getMonthlyFee()).isEqualByComparingTo("180");
        assertThat(activity(SeedData.SWIMMING_CLASS).getMonthlyFee()).isEqualByComparingTo("180");
        assertThat(activityClassRepository.findPriceHistory(SeedData.SWIMMING_CLASS)).extracting(BigDecimal::intValue).containsExactly(180, 200);
        assertThat(DbFixtures.text(
            "SELECT valid_from FROM activity_prices WHERE activity_id = ? AND monthly_fee = ?",
            SeedData.SWIMMING_CLASS.toString(),
            new BigDecimal("200")
        )).isEqualTo(nextMonth.toString());
    }

    @Test
    @DisplayName("substitui a mensalidade agendada quando muda de novo no mesmo mês")
    void shouldReplaceScheduledFee() {
        changeFee(activity(SeedData.SWIMMING_CLASS), "200");
        changeFee(activity(SeedData.SWIMMING_CLASS), "220");

        assertThat(activityClassRepository.findPriceHistory(SeedData.SWIMMING_CLASS)).extracting(BigDecimal::intValue)
                .containsExactly(180, 220);
        assertThat(activityClassRepository.findLatestPrice(SeedData.SWIMMING_CLASS, BigDecimal.ONE)).isEqualByComparingTo("220");
    }

    @Test
    @DisplayName("não cria histórico de preço ao salvar com a mesma mensalidade")
    void shouldNotCreateHistoryWhenFeeDoesNotChange() {
        changeFee(activity(SeedData.SWIMMING_CLASS), "180");

        assertThat(DbFixtures.count("SELECT COUNT(*) FROM activity_prices WHERE activity_id = ?",
                SeedData.SWIMMING_CLASS.toString())).isEqualTo(1);
    }

    @Test
    @DisplayName("registra a mensalidade original antes da primeira mudança de preço")
    void shouldCreateInitialHistoryForLegacyActivity() {
        UUID legacy = DbFixtures.insertActivity(SeedData.SPORT_JUDO, SeedData.ROOM_JUDO, SeedData.TRAINER_CARLOS,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 10, new BigDecimal("90"), true, DayOfWeek.FRIDAY);

        changeFee(activity(legacy), "95");

        assertThat(activityClassRepository.findPriceHistory(legacy)).extracting(BigDecimal::intValue).containsExactly(90, 95);
    }

    @Test
    @DisplayName("usa o valor padrão quando não há histórico de preço")
    void shouldReturnFallbackPrice() {
        UUID legacy = DbFixtures.insertActivity(SeedData.SPORT_JUDO, SeedData.ROOM_JUDO, SeedData.TRAINER_CARLOS,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 10, new BigDecimal("90"), true, DayOfWeek.FRIDAY);

        assertThat(activityClassRepository.findLatestPrice(legacy, new BigDecimal("77"))).isEqualByComparingTo("77");
    }

    @Test
    @DisplayName("faz COUNT de alunos distintos só com matrícula ativa")
    void shouldCountOnlyActiveStudents() {
        DbFixtures.fillActivity(SeedData.SWIMMING_CLASS, SeedData.SWIMMING_FEE, 2);
        UUID canceled = DbFixtures.insertEnrollment(DbFixtures.insertCustomer(), true);
        DbFixtures.insertEnrollmentActivity(canceled, SeedData.SWIMMING_CLASS, SeedData.SWIMMING_FEE,
            LocalDate.now().minusMonths(2), LocalDate.now().minusMonths(1), false);
        UUID inactiveEnrollment = DbFixtures.insertEnrollment(DbFixtures.insertCustomer(), false);
        DbFixtures.insertEnrollmentActivity(inactiveEnrollment, SeedData.SWIMMING_CLASS, SeedData.SWIMMING_FEE,
            LocalDate.now(), null, true);

        assertThat(row(SeedData.SWIMMING_CLASS, false).enrolledCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("retorna zero alunos para atividade sem matrícula (LEFT JOIN)")
    void shouldReturnZeroWithoutEnrollments() {
        ActivityClassRow row = row(SeedData.SWIMMING_CLASS, false);

        assertThat(row.enrolledCount()).isZero();
        assertThat(row.sportName()).isEqualTo(SeedData.SWIMMING_NAME);
        assertThat(row.roomName()).isEqualTo("Piscina");
        assertThat(row.trainerName()).isEqualTo(SeedData.TRAINER_JULIANA_NAME);
        assertThat(row.weekdays()).isEqualTo("MONDAY");
    }

    @Test
    @DisplayName("filtra a listagem de atividades pela flag de ativa")
    void shouldFilterByActive() {
        UUID inactive = DbFixtures.insertActivity(SeedData.SPORT_JUDO, SeedData.ROOM_JUDO, SeedData.TRAINER_CARLOS,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 10, new BigDecimal("90"), false, DayOfWeek.FRIDAY);

        assertThat(activityQueries.findAll(true)).extracting(ActivityClassRow::id).containsExactly(SeedData.SWIMMING_CLASS);
        assertThat(activityQueries.findAll(false)).extracting(ActivityClassRow::id).containsExactly(inactive);
        assertThat(activityQueries.findAll(null)).hasSize(2);
    }

    @Test
    @DisplayName("ordena as atividades pelo nome da modalidade")
    void shouldOrderBySportName() {
        DbFixtures.insertActivity(SeedData.SPORT_ATHLETICS, SeedData.ROOM_TRACK, SeedData.TRAINER_CARLOS,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 10, new BigDecimal("90"), true, DayOfWeek.FRIDAY);
        DbFixtures.insertActivity(SeedData.SPORT_SOCCER, SeedData.ROOM_SOCCER_COURT, SeedData.TRAINER_MARIA,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 10, new BigDecimal("90"), true, DayOfWeek.FRIDAY);

        assertThat(activityQueries.findAll(null)).extracting(ActivityClassRow::sportName)
                .containsExactly("Atletismo", "Futebol", SeedData.SWIMMING_NAME);
    }

    @Test
    @DisplayName("mostra a mensalidade vigente ao aluno e a agendada ao admin")
    void shouldChooseFeeByProfile() {
        DbFixtures.insertPrice(SeedData.SWIMMING_CLASS, new BigDecimal("250"), NEXT_MONTH);

        assertThat(row(SeedData.SWIMMING_CLASS, false).monthlyFee()).isEqualByComparingTo("180");
        assertThat(row(SeedData.SWIMMING_CLASS, true).monthlyFee()).isEqualByComparingTo("250");
        assertThat(activityQueries.findLatestPrice(SeedData.SWIMMING_CLASS, BigDecimal.ZERO)).isEqualByComparingTo("250");
    }

    @Test
    @DisplayName("usa o preço mais recente já em vigor (MAX valid_from <= hoje)")
    void shouldUseMostRecentPriceInForce() {
        DbFixtures.insertPrice(SeedData.SWIMMING_CLASS, new BigDecimal("170"), LocalDate.now().minusMonths(3));
        DbFixtures.insertPrice(SeedData.SWIMMING_CLASS, new BigDecimal("190"), LocalDate.now().minusDays(1));
        DbFixtures.insertPrice(SeedData.SWIMMING_CLASS, new BigDecimal("260"), NEXT_MONTH);

        assertThat(row(SeedData.SWIMMING_CLASS, false).monthlyFee()).isEqualByComparingTo("190");
    }

    @Test
    @DisplayName("mapeia todas as modalidades do catálogo")
    void shouldFindSports() {
        var sports = catalogQueries.findSports();

        assertThat(sports).hasSize(SeedData.SPORTS_COUNT);
        assertThat(sports).filteredOn(s -> s.id().equals(SeedData.SPORT_SWIMMING))
            .singleElement().satisfies(s -> {
                assertThat(s.name()).isEqualTo(SeedData.SWIMMING_NAME);
                assertThat(s.roomType().name()).isEqualTo("POOL");
                assertThat(s.active()).isTrue();
            });
    }

    @Test
    @DisplayName("mapeia todas as salas com capacidade")
    void shouldFindRooms() {
        assertThat(catalogQueries.findRooms()).hasSize(SeedData.ROOMS_COUNT)
            .filteredOn(r -> r.id().equals(SeedData.ROOM_GYM))
            .singleElement()
            .satisfies(r -> assertThat(r.capacity()).isEqualTo(SeedData.ROOM_GYM_CAPACITY));
    }

    @Test
    @DisplayName("mapeia professores inativos também, ordenados por nome")
    void shouldFindTrainers() {
        DbFixtures.update("UPDATE trainer SET active = 0 WHERE id = ?", SeedData.TRAINER_JOHN.toString());

        var trainers = catalogQueries.findTrainers();

        assertThat(trainers).hasSize(SeedData.TRAINERS_COUNT);
        assertThat(trainers).extracting(SqliteCatalogQueries.TrainerRow::name).isSorted();
        assertThat(trainers).filteredOn(t -> t.id().equals(SeedData.TRAINER_JOHN)).singleElement()
            .satisfies(t -> assertThat(t.active()).isFalse());
    }

    @Test
    @DisplayName("ordena modalidades sem diferenciar maiúsculas")
    void shouldOrderIgnoringCase() {
        DbFixtures.update("INSERT INTO sports (id, name, room_type) VALUES (?, 'basquete', 'COURT')", UUID.randomUUID().toString());

        List<String> names = catalogQueries.findSports().stream().map(SqliteCatalogQueries.SportRow::name).toList();

        assertThat(names.indexOf("basquete")).isLessThan(names.indexOf("Futebol"));
        assertThat(names.indexOf("Atletismo")).isLessThan(names.indexOf("basquete"));
    }
}
