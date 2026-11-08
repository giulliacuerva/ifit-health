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
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private Set<DayOfWeek> classDays;

    @BeforeEach
    public void setup() {
        customer = new Customer("teste", "teste@gmail.com");
        enrollment = new Enrollment(customer);
        classDays = Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should create enrollment when customer has no enrollment")
    void shouldCreateEnrollmentWhenCustomerHasNoEnrollment() {
        Set<DayOfWeek> classDays = Set.of(DayOfWeek.MONDAY);
        Schedule schedule = new Schedule(classDays, LocalTime.of(10, 0), LocalTime.of(11, 0));
        ActivityClass activityClass = createActivityClass(10, schedule);

        when(activityClassRepo.findById(activityClass.getId())).thenReturn(activityClass);
        when(enrollmentRepo.enroll(eq(customer), anyList())).thenAnswer(invocation -> {
            List<ActivityClass> activities = invocation.getArgument(1);
            Enrollment newEnrollment = new Enrollment(customer);
            newEnrollment.addActivities(activities);
            return newEnrollment;
        });

        Enrollment result = sut.enroll(customer, List.of(activityClass.getId()));

        assertThat(result).isNotNull();
        assertThat(result.getCustomer()).isEqualTo(customer);
        assertThat(result.getEnrollmentActivities()).hasSize(1);

        verify(enrollmentRepo).enroll(customer, List.of(activityClass));
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should enroll customer in activity")
    void shouldEnrollCustomerInActivity() {
        ActivityClass activityClass = createActivityClass(10);
        stubEnrollmentRepositoryToAddActivities(enrollment);
        when(activityClassRepo.findById(activityClass.getId())).thenReturn(activityClass);

        Enrollment result = sut.enroll(customer, List.of(activityClass.getId()));

        assertThat(result).isEqualTo(enrollment);
        assertThat(result.getCustomer()).isEqualTo(customer);
        assertThat(result.getEnrollmentActivities()).hasSize(1);

        verify(enrollmentRepo).enroll(customer, List.of(activityClass));
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("ShouldEnrollMultipleActivitiesInSameEnrollment")
    void shouldEnrollMultipleActivitiesInSameEnrollment() {
        ActivityClass activityClass = createActivityClass(10);
        Set<DayOfWeek> anotherClassDays = Set.of(DayOfWeek.THURSDAY);
        Schedule anotherSchedule = new Schedule(anotherClassDays, LocalTime.of(11, 0), LocalTime.of(12, 0));
        ActivityClass anotherActivityClass = createActivityClass(20, anotherSchedule);

        when(activityClassRepo.findById(activityClass.getId())).thenReturn(activityClass);
        when(activityClassRepo.findById(anotherActivityClass.getId())).thenReturn(anotherActivityClass);
        stubEnrollmentRepositoryToAddActivities(enrollment);

        Enrollment result = sut.enroll(
                customer,
                List.of(activityClass.getId(), anotherActivityClass.getId())
        );

        assertThat(result).isEqualTo(enrollment);
        assertThat(result.getCustomer()).isEqualTo(customer);
        assertThat(result.getEnrollmentActivities()).hasSize(2);

        verify(enrollmentRepo).enroll(customer, List.of(activityClass, anotherActivityClass));
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should reject enrollment when activity is full")
    void shouldRejectEnrollmentWhenActivityIsFull() {
        ActivityClass activityClass = createActivityClass(1);
        when(activityClassRepo.findById(activityClass.getId())).thenReturn(activityClass);
        when(enrollmentRepo.enroll(customer, List.of(activityClass)))
                .thenThrow(new CapacityIsGreaterThanAcceptedException(
                        "The activity has no available vacancies: " + activityClass.getId()
                ));

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
        stubEnrollmentRepositoryToAddActivities(enrollment);

        assertThatThrownBy(() -> sut.enroll(customer, List.of(newActivity.getId())))
                .isInstanceOf(ActivityScheduleConflictException.class);

        verify(enrollmentRepo, never()).save(enrollment);
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should reject enrollment when selected activities have conflicting schedules")
    void shouldRejectEnrollmentWhenSelectedActivitiesHaveConflictingSchedules() {
        Set<DayOfWeek> activityDays = Set.of(DayOfWeek.MONDAY);
        Schedule firstSchedule = new Schedule(activityDays, LocalTime.of(10, 0), LocalTime.of(11, 0));
        ActivityClass activity = createActivityClass(10, firstSchedule);

        Schedule secondSchedule = new Schedule(activityDays, LocalTime.of(10, 30), LocalTime.of(11, 30));
        ActivityClass anotherActivity = createActivityClass(10, secondSchedule);

        when(activityClassRepo.findById(activity.getId())).thenReturn(activity);
        when(activityClassRepo.findById(anotherActivity.getId())).thenReturn(anotherActivity);
        stubEnrollmentRepositoryToAddActivities(enrollment);

        assertThatThrownBy(() -> sut.enroll(customer, List.of(activity.getId(), anotherActivity.getId())))
                .isInstanceOf(ActivityScheduleConflictException.class);
        assertThat(enrollment.getEnrollmentActivities()).isEmpty();

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
        stubEnrollmentRepositoryToAddActivities(enrollment);

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

        assertThatThrownBy(() -> sut.enroll(customer, List.of(activityClass.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("The activity is inactive: " + activityClass.getId());

        verify(enrollmentRepo, never()).save(enrollment);
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should create a new enrollment activity and reactivate the enrollment when customer enrolls in the same activity again after cancellation")
    void shouldCreateNewEnrollmentActivityAfterCancellation() {
        ActivityClass activityClass = createActivityClass(10);
        enrollment.addActivity(activityClass);
        EnrollmentActivity previousEnrollmentActivity = enrollment.getEnrollmentActivities().getFirst();
        previousEnrollmentActivity.deactivate();
        enrollment.deactivate();

        when(activityClassRepo.findById(activityClass.getId())).thenReturn(activityClass);
        stubEnrollmentRepositoryToAddActivities(enrollment);

        sut.enroll(customer, List.of(activityClass.getId()));

        assertThat(enrollment.getEnrollmentActivities()).hasSize(2);
        assertThat(enrollment.getEnrollmentActivities().get(0).isActive()).isFalse();
        assertThat(enrollment.getEnrollmentActivities().get(1).isActive()).isTrue();
        assertThat(enrollment.isActive()).isTrue();
    }

    @Test
    @Tag("Functional")
    @Tag("UnitTest")
    @DisplayName("Should accept enrollment when schedules are exactly adjacent")
    void shouldAcceptEnrollmentWhenSchedulesAreExactlyAdjacent() {
        Set<DayOfWeek> monday = Set.of(DayOfWeek.MONDAY);
        Schedule existingSchedule = new Schedule(monday, LocalTime.of(9, 0), LocalTime.of(10, 0));
        ActivityClass existingActivity = createActivityClass(8, existingSchedule);
        enrollment.addActivity(existingActivity);

        Schedule newSchedule = new Schedule(monday, LocalTime.of(10, 0), LocalTime.of(11, 0));
        ActivityClass newActivity = createActivityClass(10, newSchedule);

        when(activityClassRepo.findById(newActivity.getId())).thenReturn(newActivity);
        stubEnrollmentRepositoryToAddActivities(enrollment);

        Enrollment result = sut.enroll(customer, List.of(newActivity.getId()));

        assertThat(result.getEnrollmentActivities()).hasSize(2);

        verify(enrollmentRepo).enroll(customer, List.of(newActivity));
    }

    @Test
    @Tag("Functional")
    @Tag("UnitTest")
    @DisplayName("Should reject enrollment when new schedule overlaps existing schedule by one minute at the beginning")
    void shouldRejectEnrollmentWhenNewScheduleOverlapsExistingScheduleByOneMinuteAtBeginning() {
        Set<DayOfWeek> monday = Set.of(DayOfWeek.MONDAY);
        Schedule existingSchedule = new Schedule(monday, LocalTime.of(9, 0), LocalTime.of(10, 0));
        ActivityClass existingActivity = createActivityClass(10, existingSchedule);
        enrollment.addActivity(existingActivity);

        Schedule newSchedule = new Schedule(monday, LocalTime.of(9, 59), LocalTime.of(11, 0));
        ActivityClass newActivity = createActivityClass(10, newSchedule);

        when(activityClassRepo.findById(newActivity.getId())).thenReturn(newActivity);
        stubEnrollmentRepositoryToAddActivities(enrollment);

        assertThatThrownBy(() -> sut.enroll(customer, List.of(newActivity.getId())))
                .isInstanceOf(ActivityScheduleConflictException.class);

        verify(enrollmentRepo, never()).save(any(Enrollment.class));
    }

    @Test
    @Tag("Functional")
    @Tag("UnitTest")
    @DisplayName("Should reject enrollment when new schedule overlaps existing schedule by one minute at the end")
    void shouldRejectEnrollmentWhenNewScheduleOverlapsExistingScheduleByOneMinuteAtEnd() {
        Set<DayOfWeek> monday = Set.of(DayOfWeek.MONDAY);
        Schedule existingSchedule = new Schedule(monday, LocalTime.of(11, 0), LocalTime.of(12, 0));
        ActivityClass existingActivity = createActivityClass(10, existingSchedule);
        enrollment.addActivity(existingActivity);

        Schedule newSchedule = new Schedule(monday, LocalTime.of(10, 0), LocalTime.of(11, 1));
        ActivityClass newActivity = createActivityClass(10, newSchedule);

        when(activityClassRepo.findById(newActivity.getId())).thenReturn(newActivity);
        stubEnrollmentRepositoryToAddActivities(enrollment);

        assertThatThrownBy(() -> sut.enroll(customer, List.of(newActivity.getId())))
                .isInstanceOf(ActivityScheduleConflictException.class);

        verify(enrollmentRepo, never()).save(any(Enrollment.class));
    }

    @Test
    @Tag("Functional")
    @Tag("UnitTest")
    @DisplayName("Should reject enrollment when new schedule is contained in existing schedule")
    void shouldRejectEnrollmentWhenNewScheduleIsContainedInExistingSchedule() {
        Set<DayOfWeek> monday = Set.of(DayOfWeek.MONDAY);
        Schedule existingSchedule = new Schedule(monday, LocalTime.of(9, 0), LocalTime.of(12, 0));
        ActivityClass existingActivity = createActivityClass(10, existingSchedule);
        enrollment.addActivity(existingActivity);

        Schedule newSchedule = new Schedule(monday, LocalTime.of(10, 0), LocalTime.of(11, 0));
        ActivityClass newActivity = createActivityClass(10, newSchedule);

        when(activityClassRepo.findById(newActivity.getId())).thenReturn(newActivity);
        stubEnrollmentRepositoryToAddActivities(enrollment);

        assertThatThrownBy(() -> sut.enroll(customer, List.of(newActivity.getId())))
                .isInstanceOf(ActivityScheduleConflictException.class);

        verify(enrollmentRepo, never()).save(any(Enrollment.class));
    }

    @Test
    @Tag("Functional")
    @Tag("UnitTest")
    @DisplayName("Should reject enrollment when new schedule contains existing schedule")
    void shouldRejectEnrollmentWhenNewScheduleContainsExistingSchedule() {
        Set<DayOfWeek> monday = Set.of(DayOfWeek.MONDAY);
        Schedule existingSchedule = new Schedule(monday, LocalTime.of(10, 0), LocalTime.of(11, 0));
        ActivityClass existingActivity = createActivityClass(10, existingSchedule);
        enrollment.addActivity(existingActivity);

        Schedule newSchedule = new Schedule(monday, LocalTime.of(9, 0), LocalTime.of(12, 0));
        ActivityClass newActivity = createActivityClass(10, newSchedule);

        when(activityClassRepo.findById(newActivity.getId())).thenReturn(newActivity);
        stubEnrollmentRepositoryToAddActivities(enrollment);

        assertThatThrownBy(() -> sut.enroll(customer, List.of(newActivity.getId())))
                .isInstanceOf(ActivityScheduleConflictException.class);

        verify(enrollmentRepo, never()).save(any(Enrollment.class));
    }

    private void stubEnrollmentRepositoryToAddActivities(Enrollment targetEnrollment) {
        when(enrollmentRepo.enroll(eq(customer), anyList())).thenAnswer(invocation -> {
            List<ActivityClass> activities = invocation.getArgument(1);
            targetEnrollment.addActivities(activities);
            return targetEnrollment;
        });
    }

    private ActivityClass createActivityClass(int activityCapacity) {
        Schedule schedule = new Schedule(classDays, LocalTime.of(10, 0), LocalTime.of(11, 0));
        Room room = new Room(UUID.randomUUID(), "Room A", RoomType.GYM, 10);
        Sport sport = new Sport(UUID.randomUUID(), "Basketball", RoomType.GYM);
        Trainer trainer = new Trainer(UUID.randomUUID(), "John Doe");
        return new ActivityClass(
                room, sport, trainer, schedule, activityCapacity, new BigDecimal("180.00")
        );
    }

    private ActivityClass createActivityClass(int activityCapacity, Schedule schedule) {
        Room room = new Room(UUID.randomUUID(), "Room A", RoomType.GYM, 10);
        Sport sport = new Sport(UUID.randomUUID(), "Basketball", RoomType.GYM);
        Trainer trainer = new Trainer(UUID.randomUUID(), "John Doe");
        return new ActivityClass(room, sport, trainer, schedule, activityCapacity, new BigDecimal("180.00"));
    }
}
