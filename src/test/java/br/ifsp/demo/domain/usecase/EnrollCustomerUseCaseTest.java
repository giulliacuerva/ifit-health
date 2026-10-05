package br.ifsp.demo.domain.usecase;

import br.ifsp.demo.domain.model.*;
import br.ifsp.demo.domain.model.enums.RoomType;
import br.ifsp.demo.domain.repository.ActivityClassRepository;
import br.ifsp.demo.domain.repository.EnrollmentRepository;
import br.ifsp.demo.exception.ActivityScheduleConflictException;
import br.ifsp.demo.exception.CapacityIsGreaterThanAcceptedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EnrollCustomerUseCaseTest {

    @InjectMocks
    private EnrollCustomerUseCase sut;
    @Mock
    private ActivityClassRepository activityClassRepo;
    @Mock
    private EnrollmentRepository enrollmentRepo;
    private Customer customer;
    private Enrollment enrollment;

    @BeforeEach
    public void setup(){
        customer = new Customer("teste", "teste@gmail.com");
        enrollment = new Enrollment(customer);
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should create enrollment when customer has no enrollment")
    void shouldCreateEnrollmentWhenCustomerHasNoEnrollment() {
        Set<DayOfWeek> classDays = Set.of(DayOfWeek.MONDAY);
        Schedule schedule = new Schedule(classDays, LocalTime.of(10, 0), LocalTime.of(11, 0));

        ActivityClass activityClass = createActivityClass(10, schedule);

        when(enrollmentRepo.findByCustomer(customer)).thenReturn(Optional.empty());
        when(activityClassRepo.findById(activityClass.getId())).thenReturn(activityClass);
        when(enrollmentRepo.findEnrolledActivitiesByActivityClass(activityClass)).thenReturn(List.of());
        when(enrollmentRepo.save(any(Enrollment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Enrollment result = sut.enroll(customer, List.of(activityClass.getId()));

        assertThat(result).isNotNull();
        assertThat(result.getCustomer()).isEqualTo(customer);
        assertThat(result.getEnrollmentActivities()).hasSize(1);

        verify(enrollmentRepo).findByCustomer(customer);
        verify(enrollmentRepo).save(any(Enrollment.class));
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should enroll customer in activity")
    void shouldEnrollCustomerInActivity() {
        Set<DayOfWeek> classDays = Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);
        Schedule schedule = new Schedule(classDays, LocalTime.of(10, 0),LocalTime.of(11, 0));
        ActivityClass activityClass = createActivityClass(10, schedule);

        List<EnrollmentActivity> enrollmentActivities = List.of();

        when(activityClassRepo.findById(activityClass.getId())).thenReturn(activityClass);
        when(enrollmentRepo.findEnrolledActivitiesByActivityClass(activityClass)).thenReturn(enrollmentActivities);
        when(enrollmentRepo.findByCustomer(customer)).thenReturn(Optional.of(enrollment));
        when(enrollmentRepo.save(enrollment)).thenReturn(enrollment);

        Enrollment result = sut.enroll(customer, List.of(activityClass.getId()));

        assertThat(result).isEqualTo(enrollment);
        assertThat(result.getCustomer()).isEqualTo(customer);
        assertThat(result.getEnrollmentActivities()).hasSize(1);

        verify(enrollmentRepo).save(enrollment);
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("ShouldEnrollMultipleActivitiesInSameEnrollment")
    void shouldEnrollMultipleActivitiesInSameEnrollment() {
        Set<DayOfWeek> classDays = Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);
        Schedule schedule = new Schedule(classDays, LocalTime.of(10, 0),LocalTime.of(11, 0));
        ActivityClass activityClass = createActivityClass(10, schedule);

        Set<DayOfWeek> anotherClassDays = Set.of(DayOfWeek.THURSDAY);
        Schedule anotherSchedule = new Schedule(anotherClassDays, LocalTime.of(11, 0), LocalTime.of(12, 0));
        ActivityClass anotherActivityClass = createActivityClass(20, anotherSchedule);

        when(activityClassRepo.findById(activityClass.getId())).thenReturn(activityClass);
        when(activityClassRepo.findById(anotherActivityClass.getId())).thenReturn(anotherActivityClass);

        when(enrollmentRepo.findEnrolledActivitiesByActivityClass(activityClass)).thenReturn(List.of());
        when(enrollmentRepo.findEnrolledActivitiesByActivityClass(anotherActivityClass)).thenReturn(List.of());
        when(enrollmentRepo.findByCustomer(customer)).thenReturn(Optional.of(enrollment));
        when(enrollmentRepo.save(enrollment)).thenReturn(enrollment);

        Enrollment result = sut.enroll(customer, List.of(activityClass.getId(),anotherActivityClass.getId()));

        assertThat(result).isEqualTo(enrollment);
        assertThat(result.getCustomer()).isEqualTo(customer);
        assertThat(result.getEnrollmentActivities()).hasSize(2);

        verify(enrollmentRepo).save(enrollment);
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should reject enrollment when activity is full")
    void shouldRejectEnrollmentWhenActivityIsFull() {
        Set<DayOfWeek> classDays = Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);
        Schedule schedule = new Schedule(classDays, LocalTime.of(10, 0),LocalTime.of(11, 0));
        ActivityClass activityClass = createActivityClass(1, schedule);

        EnrollmentActivity enrollmentActivity = new EnrollmentActivity(activityClass, new BigDecimal("200.00"));

        when(activityClassRepo.findById(activityClass.getId())).thenReturn(activityClass);
        when(enrollmentRepo.findEnrolledActivitiesByActivityClass(activityClass))
                .thenReturn(List.of(enrollmentActivity));

        assertThatThrownBy(() -> sut.enroll(customer, List.of(activityClass.getId())))
                .isInstanceOf(CapacityIsGreaterThanAcceptedException.class);

        verify(enrollmentRepo, never()).save(enrollment);
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should reject enrollment when activity conflicts with customer schedule")
    void shouldRejectEnrollmentWhenActivityConflictsWithCustomerSchedule() {
        Set<DayOfWeek> activityDays = Set.of(DayOfWeek.MONDAY);
        Schedule existingSchedule = new Schedule(activityDays, LocalTime.of(10, 0), LocalTime.of(11, 0));
        ActivityClass existingActivity = createActivityClass(10, existingSchedule);
        enrollment.addActivity(existingActivity);

        Schedule newSchedule = new Schedule(activityDays, LocalTime.of(10, 30), LocalTime.of(11, 30));
        ActivityClass newActivity = createActivityClass(10, newSchedule);

        when(activityClassRepo.findById(newActivity.getId())).thenReturn(newActivity);
        when(enrollmentRepo.findEnrolledActivitiesByActivityClass(newActivity)).thenReturn(List.of());
        when(enrollmentRepo.findByCustomer(customer)).thenReturn(Optional.of(enrollment));

        assertThatThrownBy(() -> sut.enroll(customer, List.of(newActivity.getId())))
                .isInstanceOf(ActivityScheduleConflictException.class);

        verify(enrollmentRepo, never()).save(enrollment);
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should reject enrollment when selected activities have conflicting schedules")
    void shouldRejectEnrollmentWhenSelectedActivitiesHaveConflictingSchedules() {
        Set<DayOfWeek> ActivityDays = Set.of(DayOfWeek.MONDAY);
        Schedule firstSchedule = new Schedule(ActivityDays, LocalTime.of(10, 0), LocalTime.of(11, 0));
        ActivityClass activity = createActivityClass(10, firstSchedule);

        Schedule secondSchedule = new Schedule(ActivityDays, LocalTime.of(10, 30), LocalTime.of(11, 30));
        ActivityClass anotherActivity = createActivityClass(10, secondSchedule);

        when(activityClassRepo.findById(activity.getId())).thenReturn(activity);
        when(activityClassRepo.findById(anotherActivity.getId())).thenReturn(anotherActivity);
        when(enrollmentRepo.findEnrolledActivitiesByActivityClass(activity)).thenReturn(List.of());
        when(enrollmentRepo.findEnrolledActivitiesByActivityClass(anotherActivity)).thenReturn(List.of());
        when(enrollmentRepo.findByCustomer(customer)).thenReturn(Optional.of(enrollment));

        assertThatThrownBy(() -> sut.enroll(customer, List.of(activity.getId(), anotherActivity.getId())))
                .isInstanceOf(ActivityScheduleConflictException.class);

        verify(enrollmentRepo, never()).save(any(Enrollment.class));
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should rejected enrollment when customer is already enrolled in activity")
    void shouldRejectedEnrollmentWhenCustomerIsAlreadyEnrolledInActivity() {
        Set<DayOfWeek> classDays = Set.of(DayOfWeek.MONDAY);
        Schedule schedule = new Schedule(classDays, LocalTime.of(10, 0), LocalTime.of(11, 0));
        ActivityClass activityClass = createActivityClass(10, schedule);
        enrollment.addActivity(activityClass);

        when(activityClassRepo.findById(activityClass.getId())).thenReturn(activityClass);
        when(enrollmentRepo.findEnrolledActivitiesByActivityClass(activityClass)).thenReturn(List.of());
        when(enrollmentRepo.findByCustomer(customer)).thenReturn(Optional.of(enrollment));

        assertThatThrownBy(() -> sut.enroll(customer, List.of(activityClass.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("The customer is already enrolled in this activity");

        verify(enrollmentRepo, never()).save(any(Enrollment.class));
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should reject enrollment when activity is inactive")
    void shouldRejectEnrollmentWhenActivityIsInactive() {
        Set<DayOfWeek> classDays = Set.of(DayOfWeek.MONDAY);
        Schedule schedule = new Schedule(classDays, LocalTime.of(10, 0), LocalTime.of(11, 0));
        ActivityClass activityClass = createActivityClass(10, schedule);
        activityClass.deactivate();

        when(activityClassRepo.findById(activityClass.getId())).thenReturn(activityClass);
        when(enrollmentRepo.findByCustomer(customer)).thenReturn(Optional.of(enrollment));

        assertThatThrownBy(() -> sut.enroll(customer, List.of(activityClass.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("The activity is inactive");

        verify(enrollmentRepo, never()).save(any(Enrollment.class));
    }

    private ActivityClass createActivityClass(int activityCapacity, Schedule schedule) {
        Room room = new Room(UUID.randomUUID(), "Room A", RoomType.GYM, 10);
        Sport sport = new Sport(UUID.randomUUID(), "Basketball", RoomType.GYM);
        Trainer trainer = new Trainer(UUID.randomUUID(), "John Doe");
        return new ActivityClass(
                room, sport, trainer, schedule, activityCapacity, new BigDecimal("180.00")
        );
    }
}
