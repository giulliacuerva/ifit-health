package br.ifsp.demo.domain.usecase;

import br.ifsp.demo.domain.model.*;
import br.ifsp.demo.domain.model.enums.RoomType;
import br.ifsp.demo.domain.repository.EnrollmentRepository;
import br.ifsp.demo.domain.usecase.dto.ActivityReport;
import br.ifsp.demo.domain.usecase.dto.ActivityReportItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import br.ifsp.demo.domain.repository.ActivityClassRepository;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateActivityReportUseCaseTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private ActivityClassRepository activityClassRepository;

    @InjectMocks
    private GenerateActivityReportUseCase sut;
    private ActivityClass activityClass;
    private Sport swimming;
    private ActivityClass swimmingActivity;
    private ActivityClass judoActivity;

    @BeforeEach
    void setUp() {
        Schedule schedule = new Schedule(Set.of(DayOfWeek.MONDAY), LocalTime.of(10, 0), LocalTime.of(11, 0));
        activityClass = createActivityClass(10, schedule);
        swimming = new Sport(UUID.randomUUID(), "Swimming", RoomType.POOL);
        Sport judo = new Sport(UUID.randomUUID(), "Judo", RoomType.TATAMI);
        Schedule swimmingSchedule = new Schedule(Set.of(DayOfWeek.MONDAY), LocalTime.of(10, 0), LocalTime.of(11, 0));
        Schedule judoSchedule = new Schedule(Set.of(DayOfWeek.TUESDAY), LocalTime.of(14, 0), LocalTime.of(15, 0));
        swimmingActivity = createActivityClass(swimming, 10, swimmingSchedule, new BigDecimal("180.00"));
        judoActivity = createActivityClass(judo, 10, judoSchedule, new BigDecimal("200.00"));
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should generate report with active enrollments in the period")
    void shouldGenerateReportWithActiveEnrollmentsInPeriod() {
        LocalDate startDate = LocalDate.of(2026, 10, 1);
        LocalDate endDate = LocalDate.of(2026, 10, 31);

        EnrollmentActivity enrollmentActivity1 = new EnrollmentActivity(activityClass, new BigDecimal("180.00"));
        EnrollmentActivity enrollmentActivity2 = new EnrollmentActivity(activityClass, new BigDecimal("180.00"));

        when(activityClassRepository.findAll())
                .thenReturn(List.of(activityClass));

        when(enrollmentRepository.findEnrollmentActivitiesByPeriod(startDate, endDate))
                .thenReturn(List.of(enrollmentActivity1, enrollmentActivity2));

        ActivityReport result = sut.generate(startDate, endDate);

        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(1);

        ActivityReportItem item = result.getItems().getFirst();

        assertThat(item.getActivityClass()).isEqualTo(activityClass);
        assertThat(item.getEnrollmentCount()).isEqualTo(2);
        assertThat(item.getRevenue()).isEqualByComparingTo("360.00");

        verify(enrollmentRepository).findEnrollmentActivitiesByPeriod(startDate, endDate);

    }


    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should generate report only with activities from selected sport")
    void shouldGenerateReportOnlyWithSelectedSport() {
        LocalDate startDate = LocalDate.of(2026, 10, 1);
        LocalDate endDate = LocalDate.of(2026, 10, 31);

        EnrollmentActivity swimmingEnrollment1 = new EnrollmentActivity(swimmingActivity, new BigDecimal("180.00"));
        EnrollmentActivity swimmingEnrollment2 = new EnrollmentActivity(swimmingActivity, new BigDecimal("180.00"));
        EnrollmentActivity judoEnrollment1 = new EnrollmentActivity(judoActivity, new BigDecimal("200.00"));
        EnrollmentActivity judoEnrollment2 = new EnrollmentActivity(judoActivity, new BigDecimal("200.00"));

        when(enrollmentRepository
                .findActiveEnrollmentActivitiesByStartDateBetweenAndSport(startDate, endDate, swimming))
                .thenReturn(List.of(swimmingEnrollment1, swimmingEnrollment2));

        ActivityReport result = sut.generate(startDate, endDate, swimming);

        assertThat(result).isNotNull();
        assertThat(result.getStartDate()).isEqualTo(startDate);
        assertThat(result.getEndDate()).isEqualTo(endDate);
        assertThat(result.getItems()).hasSize(1);

        ActivityReportItem item = result.getItems().getFirst();

        assertThat(item.getActivityClass()).isEqualTo(swimmingActivity);
        assertThat(item.getEnrollmentCount()).isEqualTo(2);
        assertThat(item.getRevenue()).isEqualByComparingTo("360.00");

        verify(enrollmentRepository).findActiveEnrollmentActivitiesByStartDateBetweenAndSport(startDate, endDate, swimming);
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should generate report only with activities from selected day")
    void shouldGenerateReportOnlyWithSelectedDay() {
        LocalDate startDate = LocalDate.of(2026, 10, 1);
        LocalDate endDate = LocalDate.of(2026, 10, 31);

        DayOfWeek dayFilter = DayOfWeek.MONDAY;

        EnrollmentActivity swimmingEnrollment1 = new EnrollmentActivity(swimmingActivity, new BigDecimal("180.00"));

        EnrollmentActivity swimmingEnrollment2 =
                new EnrollmentActivity(swimmingActivity, new BigDecimal("180.00"));

        EnrollmentActivity judoEnrollment = new EnrollmentActivity(judoActivity, new BigDecimal("200.00"));

        when(enrollmentRepository
                .findActiveEnrollmentActivitiesByStartDateBetweenAndDayOfWeek(startDate, endDate, dayFilter))
                .thenReturn(List.of(swimmingEnrollment1, swimmingEnrollment2));

        ActivityReport result = sut.generate(startDate, endDate, dayFilter);

        assertThat(result).isNotNull();
        assertThat(result.getStartDate()).isEqualTo(startDate);
        assertThat(result.getEndDate()).isEqualTo(endDate);
        assertThat(result.getItems()).hasSize(1);

        ActivityReportItem item = result.getItems().getFirst();

        assertThat(item.getActivityClass()).isEqualTo(swimmingActivity);
        assertThat(item.getEnrollmentCount()).isEqualTo(2);
        assertThat(item.getRevenue()).isEqualByComparingTo("360.00");

        verify(enrollmentRepository).findActiveEnrollmentActivitiesByStartDateBetweenAndDayOfWeek(startDate, endDate, dayFilter);
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName(
            "Should generate report only with activities that match sport and day filters"
    )
    void shouldGenerateReportWithSportAndDayFilters() {
        LocalDate startDate = LocalDate.of(2026, 10, 1);
        LocalDate endDate = LocalDate.of(2026, 10, 31);
        DayOfWeek dayFilter = DayOfWeek.MONDAY;

        EnrollmentActivity swimmingMondayEnrollment = new EnrollmentActivity(swimmingActivity, new BigDecimal("180.00"));
        EnrollmentActivity judoTuesdayEnrollment = new EnrollmentActivity(judoActivity, new BigDecimal("200.00"));

        when(enrollmentRepository
                .findActiveEnrollmentActivitiesByStartDateBetweenAndSport(startDate, endDate, swimming))
                .thenReturn(List.of(swimmingMondayEnrollment));

        ActivityReport result = sut.generate(startDate, endDate, swimming, dayFilter);

        assertThat(result).isNotNull();
        assertThat(result.getStartDate()).isEqualTo(startDate);
        assertThat(result.getEndDate()).isEqualTo(endDate);
        assertThat(result.getItems()).hasSize(1);

        ActivityReportItem item = result.getItems().getFirst();

        assertThat(item.getActivityClass()).isEqualTo(swimmingActivity);
        assertThat(item.getEnrollmentCount()).isEqualTo(1);
        assertThat(item.getRevenue()).isEqualByComparingTo("180.00");

        verify(enrollmentRepository).findActiveEnrollmentActivitiesByStartDateBetweenAndSport(startDate, endDate, swimming);
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should include activities without enrollments with zero count and revenue")
    void shouldIncludeActivitiesWithoutEnrollments() {
        LocalDate startDate = LocalDate.of(2026, 10, 1);
        LocalDate endDate = LocalDate.of(2026, 10, 31);

        when(activityClassRepository.findAll())
                .thenReturn(List.of(activityClass));

        when(enrollmentRepository.findEnrollmentActivitiesByPeriod(startDate, endDate))
                .thenReturn(List.of());

        ActivityReport result = sut.generate(startDate, endDate);

        assertThat(result.getItems()).hasSize(1);

        ActivityReportItem item = result.getItems().getFirst();

        assertThat(item.getActivityClass()).isEqualTo(activityClass);
        assertThat(item.getEnrollmentCount()).isZero();
        assertThat(item.getRevenue()).isEqualByComparingTo("0.00");
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should calculate revenue for months in which enrollment was active")
    void shouldCalculateRevenueForMonthsEnrollmentWasActive() {
        LocalDate startDate = LocalDate.of(2026, 9, 1);
        LocalDate endDate = LocalDate.of(2026, 10, 31);

        EnrollmentActivity enrollmentActivity = new EnrollmentActivity(
                activityClass,
                new BigDecimal("180.00"),
                LocalDate.of(2026, 9, 1)
        );

        enrollmentActivity.deactivate(
                LocalDate.of(2026, 10, 15)
        );

        when(activityClassRepository.findAll())
                .thenReturn(List.of(activityClass));

        when(enrollmentRepository.findEnrollmentActivitiesByPeriod(
                startDate,
                endDate
        )).thenReturn(List.of(enrollmentActivity));

        ActivityReport result = sut.generate(startDate, endDate);

        ActivityReportItem item = result.getItems().getFirst();

        assertThat(item.getRevenue())
                .isEqualByComparingTo("360.00");
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should keep closed period report unchanged after later enrollment cancellation")
    void shouldKeepClosedPeriodReportUnchangedAfterLaterCancellation() {
        LocalDate startDate = LocalDate.of(2026, 10, 1);
        LocalDate endDate = LocalDate.of(2026, 10, 31);

        EnrollmentActivity enrollmentActivity = new EnrollmentActivity(
                activityClass,
                new BigDecimal("180.00"),
                LocalDate.of(2026, 10, 1)
        );

        when(activityClassRepository.findAll())
                .thenReturn(List.of(activityClass));

        when(enrollmentRepository.findEnrollmentActivitiesByPeriod(
                startDate,
                endDate
        ))
                .thenReturn(List.of(enrollmentActivity))
                .thenReturn(List.of(enrollmentActivity));

        ActivityReport reportBeforeCancellation =
                sut.generate(startDate, endDate);

        enrollmentActivity.deactivate(
                LocalDate.of(2026, 11, 5)
        );

        ActivityReport reportAfterCancellation =
                sut.generate(startDate, endDate);

        assertThat(reportBeforeCancellation.getItems().getFirst().getRevenue())
                .isEqualByComparingTo("180.00");

        assertThat(reportAfterCancellation.getItems().getFirst().getRevenue())
                .isEqualByComparingTo("180.00");
    }


    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should calculate average occupancy when enrollment count varies during the period")
    void shouldCalculateAverageOccupancyWhenEnrollmentCountVaries() {
        LocalDate startDate = LocalDate.of(2026, 10, 1);
        LocalDate endDate = LocalDate.of(2026, 10, 31);

        EnrollmentActivity enrollment1 = new EnrollmentActivity(
                activityClass,
                new BigDecimal("180.00"),
                LocalDate.of(2026, 10, 1)
        );

        EnrollmentActivity enrollment2 = new EnrollmentActivity(
                activityClass,
                new BigDecimal("180.00"),
                LocalDate.of(2026, 10, 15)
        );

        when(activityClassRepository.findAll())
                .thenReturn(List.of(activityClass));

        when(enrollmentRepository.findEnrollmentActivitiesByPeriod(
                startDate,
                endDate
        )).thenReturn(List.of(enrollment1, enrollment2));

        ActivityReport result = sut.generate(startDate, endDate);

        ActivityReportItem item = result.getItems().getFirst();

        assertThat(item.getAverageOccupancy())
                .isEqualByComparingTo("15.00");
    }

    @Test
    @Tag("Functional")
    @Tag("UnitTest")
    @DisplayName("Should not include activities from different sport")
    void shouldNotIncludeActivitiesFromDifferentSport() {
        LocalDate startDate = LocalDate.of(2026, 10, 1);
        LocalDate endDate = LocalDate.of(2026, 10, 31);

        EnrollmentActivity judoEnrollment = new EnrollmentActivity(judoActivity, new BigDecimal("200.00"));

        when(enrollmentRepository.findActiveEnrollmentActivitiesByStartDateBetweenAndSport(startDate, endDate, swimming))
                .thenReturn(List.of());

        ActivityReport result = sut.generate(startDate, endDate, swimming);

        assertThat(result).isNotNull();
        assertThat(result.getItems()).isEmpty();

        verify(enrollmentRepository)
                .findActiveEnrollmentActivitiesByStartDateBetweenAndSport(startDate, endDate, swimming);
    }

    private ActivityClass createActivityClass(int activityCapacity, Schedule schedule) {
        Room room = new Room(UUID.randomUUID(), "Room A", RoomType.GYM, 10);
        Sport sport = new Sport(UUID.randomUUID(), "Basketball", RoomType.GYM);
        Trainer trainer = new Trainer(UUID.randomUUID(), "John Doe");
        return new ActivityClass(room, sport, trainer, schedule, activityCapacity, new BigDecimal("180.00"));
    }

    private ActivityClass createActivityClass(Sport sport, int activityCapacity, Schedule schedule, BigDecimal monthlyFee) {
        Room room = new Room(UUID.randomUUID(), "Room A", sport.getRoomType(), 10);
        Trainer trainer = new Trainer(UUID.randomUUID(), "John Doe");
        return new ActivityClass(room, sport, trainer, schedule, activityCapacity, monthlyFee);
    }

}

