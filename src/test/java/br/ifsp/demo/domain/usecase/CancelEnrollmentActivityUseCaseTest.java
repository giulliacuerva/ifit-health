package br.ifsp.demo.domain.usecase;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import br.ifsp.demo.domain.model.*;
import br.ifsp.demo.domain.model.enums.RoomType;
import br.ifsp.demo.domain.repository.EnrollmentRepository;
import br.ifsp.demo.exception.EnrollmentActivityAlreadyInactiveException;

import br.ifsp.demo.exception.EnrollmentActivityNotFoundException;
import br.ifsp.demo.exception.EnrollmentNotFoundException;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
public class CancelEnrollmentActivityUseCaseTest {
    @InjectMocks
    private CancelEnrollmentActivityUseCase sut;
    @Mock
    private EnrollmentRepository enrollmentRepo;

    private Customer customer;
    private Enrollment enrollment;
    private ActivityClass activityClass;

    @BeforeEach
    public void setup() {
        customer = new Customer("Test", "test@gmail.com");
        enrollment = new Enrollment(customer);
        activityClass = createActivityClass(1);
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should cancel the selected activity enrollment")
    void shouldCancelSelectedActivityEnrollment() {
        Schedule newSchedule = new Schedule(Set.of(DayOfWeek.THURSDAY), LocalTime.of(10, 0), LocalTime.of(11, 0));
        ActivityClass otherActivity = createActivityClass(10, newSchedule);
        enrollment.addActivity(activityClass);
        enrollment.addActivity(otherActivity);

        EnrollmentActivity enrollmentActivityToCancel = enrollment.getEnrollmentActivities().get(0);
        EnrollmentActivity otherEnrollmentActivity = enrollment.getEnrollmentActivities().get(1);

        when(enrollmentRepo.findByCustomer(customer)).thenReturn(Optional.ofNullable(enrollment));

        sut.cancelActivity(customer, enrollmentActivityToCancel.getId());

        assertThat(enrollmentActivityToCancel.isActive()).isFalse();
        assertThat(otherEnrollmentActivity.isActive()).isTrue();
        assertThat(enrollment.isActive()).isTrue();
        verify(enrollmentRepo).save(enrollment);
    }
    private ActivityClass createActivityClass(int activityCapacity) {
        Room room = new Room(UUID.randomUUID(), "Room A", RoomType.GYM, 10);
        Sport sport = new Sport(UUID.randomUUID(), "Basketball", RoomType.GYM);
        Trainer trainer = new Trainer(UUID.randomUUID(), "John Doe");
        Set<DayOfWeek> classDays = Set.of(DayOfWeek.MONDAY);
        Schedule schedule = new Schedule(classDays, LocalTime.of(10, 0), LocalTime.of(11, 0));
        return new ActivityClass(room, sport, trainer, schedule, activityCapacity, new BigDecimal("180.00"));
    }
    private ActivityClass createActivityClass(int activityCapacity, Schedule schedule) {
        Room room = new Room(UUID.randomUUID(), "Room A", RoomType.GYM, 10);
        Sport sport = new Sport(UUID.randomUUID(), "Basketball", RoomType.GYM);
        Trainer trainer = new Trainer(UUID.randomUUID(), "John Doe");
        return new ActivityClass(room, sport, trainer, schedule, activityCapacity, new BigDecimal("180.00"));
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("should deactivate enrollment and enrollment activity")
    void shouldDeactivateEnrollmentAndActivity(){
        enrollment.addActivity(activityClass);
        EnrollmentActivity enrollmentActivityToCancel = enrollment.getEnrollmentActivities().getFirst();
        when(enrollmentRepo.findByCustomer(customer)).thenReturn(Optional.ofNullable(enrollment));
        sut.cancelActivity(customer, enrollmentActivityToCancel.getId());
        assertThat(enrollmentActivityToCancel.isActive()).isFalse();
        assertThat(enrollment.isActive()).isFalse();
        verify(enrollmentRepo).save(enrollment);
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should not cancel an enrollment activity when it is already inactive")
    void shouldNotCancelEnrollmentActivityWhenAlreadyInactive() {
        enrollment.addActivity(activityClass);
        EnrollmentActivity enrollmentActivity = enrollment.getEnrollmentActivities().getFirst();
        enrollmentActivity.deactivate();
        when(enrollmentRepo.findByCustomer(customer)).thenReturn(Optional.ofNullable(enrollment));
        assertThatThrownBy(() -> sut.cancelActivity(customer, enrollmentActivity.getId()))
                .isInstanceOf(EnrollmentActivityAlreadyInactiveException.class);
    }

    @Test
    @Tag("Functional")
    @Tag("UnitTest")
    @DisplayName("Should not cancel activity when enrollment activity is not found")
    void shouldNotCancelActivityWhenEnrollmentActivityIsNotFound() {
        UUID invalidActivityId = UUID.randomUUID();
        when(enrollmentRepo.findByCustomer(customer)).thenReturn(Optional.ofNullable(enrollment));
        assertThatThrownBy(() -> sut.cancelActivity(customer, invalidActivityId))
                .isInstanceOf(EnrollmentActivityNotFoundException.class);
    }

    @Test
    @Tag("Functional")
    @Tag("UnitTest")
    @DisplayName("Should not cancel activity when customer has no enrollment")
    void shouldNotCancelActivityWhenCustomerHasNoEnrollment() {
        UUID activityId = UUID.randomUUID();
        when(enrollmentRepo.findByCustomer(customer)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> sut.cancelActivity(customer, activityId))
                .isInstanceOf(EnrollmentNotFoundException.class);
    }
}