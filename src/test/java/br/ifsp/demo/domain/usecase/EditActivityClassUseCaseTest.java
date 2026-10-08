package br.ifsp.demo.domain.usecase;

import br.ifsp.demo.domain.model.*;
import br.ifsp.demo.domain.repository.ActivityClassRepository;
import br.ifsp.demo.domain.repository.EnrollmentRepository;
import br.ifsp.demo.exception.ActivityScheduleConflictException;
import br.ifsp.demo.exception.TrainerScheduleConflictException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import br.ifsp.demo.domain.model.enums.RoomType;
import java.util.List;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;
import br.ifsp.demo.exception.RoomScheduleConflictException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EditActivityClassUseCaseTest {

    @Mock
    private ActivityClassRepository activityClassRepo;
    @Mock
    private EnrollmentRepository enrollmentRepo;
    @InjectMocks
    private EditActivityClassUseCase sut;

    private Trainer trainer;
    private Room room;
    private Sport sport;
    private Schedule schedule;
    private ActivityClass activityClass;
    private Schedule otherSchedule;
    private ActivityClass enrolledActivity;

    @BeforeEach
    void setup() {
        trainer = new Trainer(UUID.randomUUID(), "John Doe");
        room = new Room(UUID.randomUUID(), "Room A", RoomType.GYM, 10);
        sport = new Sport(UUID.randomUUID(), "Basketball", RoomType.GYM);

        Set<DayOfWeek> classDays =
                Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);

        schedule = new Schedule(
                classDays,
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        activityClass = new ActivityClass(
                room,
                sport,
                trainer,
                schedule,
                10,
                BigDecimal.valueOf(200)
        );

        otherSchedule = new Schedule(
                Set.of(DayOfWeek.TUESDAY),
                LocalTime.of(14, 0),
                LocalTime.of(15, 0)
        );

        enrolledActivity = new ActivityClass(
                room,
                sport,
                trainer,
                otherSchedule,
                10,
                BigDecimal.valueOf(200)
        );
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should edit ActivityClass when there is no room or trainer conflict")
    void shouldEditActivityClassWhenThereIsNoConflict() {

        when(activityClassRepo.findById(activityClass.getId()))
                .thenReturn(activityClass);

        when(activityClassRepo.save(activityClass))
                .thenReturn(activityClass);


        ActivityClass result = sut.edit(
                activityClass.getId(),
                room,
                sport,
                trainer,
                schedule,
                10,
                BigDecimal.valueOf(250)
        );

        assertThat(result.getMonthlyFee())
                .isEqualByComparingTo(BigDecimal.valueOf(250));

        verify(activityClassRepo).save(activityClass);
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should reject edit when room has schedule conflict")
    void shouldRejectEditWhenRoomHasScheduleConflict() {
        ActivityClass otherActivity = new ActivityClass(
                room,
                sport,
                trainer,
                schedule,
                10,
                BigDecimal.valueOf(200)
        );

        when(activityClassRepo.findById(activityClass.getId()))
                .thenReturn(activityClass);

        when(activityClassRepo.findByRoom(room))
                .thenReturn(List.of(otherActivity));

        assertThatThrownBy(() -> sut.edit(
                activityClass.getId(),
                room,
                sport,
                trainer,
                schedule,
                10,
                BigDecimal.valueOf(250)
        ))
                .isInstanceOf(RoomScheduleConflictException.class);

        verify(activityClassRepo, never()).save(any());
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should edit activity without conflicting with itself")
    void shouldEditActivityWithoutConflictingWithItself() {

        when(activityClassRepo.findById(activityClass.getId()))
                .thenReturn(activityClass);

        when(activityClassRepo.findByRoom(room))
                .thenReturn(List.of(activityClass));

        when(activityClassRepo.save(activityClass))
                .thenReturn(activityClass);

        ActivityClass result = sut.edit(
                activityClass.getId(),
                room,
                sport,
                trainer,
                schedule,
                10,
                BigDecimal.valueOf(250)
        );

        assertThat(result.getMonthlyFee())
                .isEqualByComparingTo(BigDecimal.valueOf(250));

        verify(activityClassRepo).save(activityClass);
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should reject edit when trainer has schedule conflict")
    void shouldRejectEditWhenTrainerHasScheduleConflict() {

        ActivityClass otherActivity = new ActivityClass(
                room,
                sport,
                trainer,
                schedule,
                10,
                BigDecimal.valueOf(200)
        );

        when(activityClassRepo.findById(activityClass.getId()))
                .thenReturn(activityClass);

        when(activityClassRepo.findByTrainer(trainer))
                .thenReturn(List.of(otherActivity));

        assertThatThrownBy(() -> sut.edit(
                activityClass.getId(),
                room,
                sport,
                trainer,
                schedule,
                10,
                BigDecimal.valueOf(250)
        ))
                .isInstanceOf(TrainerScheduleConflictException.class);

        verify(activityClassRepo, never()).save(any());
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should reject edit when new schedule conflicts with enrolled student's schedule")
    void shouldRejectEditWhenStudentHasScheduleConflict() {

        Schedule conflictingSchedule = new Schedule(
                Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                LocalTime.of(10, 30),
                LocalTime.of(11, 30)
        );

        ActivityClass conflictingActivity = new ActivityClass(
                room,
                sport,
                trainer,
                conflictingSchedule,
                10,
                BigDecimal.valueOf(200)
        );

        EnrollmentActivity enrollmentActivity =
                new EnrollmentActivity(
                        conflictingActivity,
                        BigDecimal.valueOf(200)
                );

        when(activityClassRepo.findById(activityClass.getId()))
                .thenReturn(activityClass);

        when(activityClassRepo.findByRoom(room))
                .thenReturn(List.of());

        when(activityClassRepo.findByTrainer(trainer))
                .thenReturn(List.of());

        when(enrollmentRepo.findEnrolledActivitiesByActivityClass(activityClass))
                .thenReturn(List.of(enrollmentActivity));

        assertThatThrownBy(() -> sut.edit(
                activityClass.getId(),
                room,
                sport,
                trainer,
                conflictingSchedule,
                10,
                BigDecimal.valueOf(250)
        ))
                .isInstanceOf(ActivityScheduleConflictException.class);

        verify(activityClassRepo, never()).save(any());
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should reject edit when capacity is lower than enrolled students")
    void shouldRejectEditWhenCapacityIsLowerThanEnrolledStudents() {

        EnrollmentActivity enrollmentActivity1 =
                new EnrollmentActivity(enrolledActivity, BigDecimal.valueOf(200));

        EnrollmentActivity enrollmentActivity2 =
                new EnrollmentActivity(enrolledActivity, BigDecimal.valueOf(200));

        EnrollmentActivity enrollmentActivity3 =
                new EnrollmentActivity(enrolledActivity, BigDecimal.valueOf(200));

        when(activityClassRepo.findById(activityClass.getId()))
                .thenReturn(activityClass);

        when(activityClassRepo.findByRoom(room))
                .thenReturn(List.of());

        when(activityClassRepo.findByTrainer(trainer))
                .thenReturn(List.of());

        when(enrollmentRepo.findEnrolledActivitiesByActivityClass(activityClass))
                .thenReturn(List.of(
                        enrollmentActivity1,
                        enrollmentActivity2,
                        enrollmentActivity3
                ));

        assertThatThrownBy(() -> sut.edit(
                activityClass.getId(),
                room,
                sport,
                trainer,
                schedule,
                2,
                BigDecimal.valueOf(250)
        ))
                .isInstanceOf(IllegalStateException.class);

        verify(activityClassRepo, never()).save(any());
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should keep previous monthly fee for existing enrollment")
    void shouldKeepPreviousMonthlyFeeForExistingEnrollment() {


        Schedule enrollmentSchedule = new Schedule(
                Set.of(DayOfWeek.TUESDAY),
                LocalTime.of(14, 0),
                LocalTime.of(15, 0)
        );

        ActivityClass enrolledActivity = new ActivityClass(
                room,
                sport,
                trainer,
                enrollmentSchedule,
                10,
                BigDecimal.valueOf(200)
        );

        EnrollmentActivity enrollmentActivity =
                new EnrollmentActivity(
                        enrolledActivity,
                        BigDecimal.valueOf(200)
                );

        when(activityClassRepo.findById(activityClass.getId()))
                .thenReturn(activityClass);

        when(activityClassRepo.findByRoom(room))
                .thenReturn(List.of());

        when(activityClassRepo.findByTrainer(trainer))
                .thenReturn(List.of());

        when(enrollmentRepo.findEnrolledActivitiesByActivityClass(activityClass))
                .thenReturn(List.of(enrollmentActivity));

        when(activityClassRepo.save(activityClass))
                .thenReturn(activityClass);

        sut.edit(
                activityClass.getId(),
                room,
                sport,
                trainer,
                schedule,
                10,
                BigDecimal.valueOf(250)
        );

        assertThat(activityClass.getMonthlyFee())
                .isEqualByComparingTo(BigDecimal.valueOf(250));

        assertThat(enrollmentActivity.getMonthlyFee())
                .isEqualByComparingTo(BigDecimal.valueOf(200));
    }

}