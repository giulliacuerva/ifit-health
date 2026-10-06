package br.ifsp.demo.domain.usecase;

import br.ifsp.demo.domain.model.*;
import br.ifsp.demo.domain.model.enums.RoomType;
import br.ifsp.demo.domain.repository.EnrollmentRepository;
import br.ifsp.demo.domain.usecase.dto.ActivityReport;
import br.ifsp.demo.domain.usecase.dto.ActivityReportItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    @DisplayName("Should generate report with active enrollments in the period")
    void shouldGenerateReportWithActiveEnrollmentsInPeriod() {
        LocalDate startDate = LocalDate.of(2026, 10, 1);
        LocalDate endDate = LocalDate.of(2026, 10, 31);

        EnrollmentActivity enrollmentActivity1 = new EnrollmentActivity(activityClass, new BigDecimal("180.00"));
        EnrollmentActivity enrollmentActivity2 = new EnrollmentActivity(activityClass, new BigDecimal("180.00"));

        when(enrollmentRepository.findActiveEnrollmentActivitiesByStartDateBetween(startDate, endDate))
                .thenReturn(List.of(enrollmentActivity1, enrollmentActivity2));

        ActivityReport result = sut.generate(startDate, endDate);

        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(1);

        ActivityReportItem item = result.getItems().getFirst();

        assertThat(item.getActivityClass()).isEqualTo(activityClass);
        assertThat(item.getEnrollmentCount()).isEqualTo(2);
        assertThat(item.getRevenue()).isEqualByComparingTo("360.00");

        verify(enrollmentRepository).findActiveEnrollmentActivitiesByStartDateBetween(startDate, endDate);
    }

    @Test
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