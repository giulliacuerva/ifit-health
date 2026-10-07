package br.ifsp.demo.domain.usecase;

import java.math.BigDecimal;
import java.util.*;
import java.time.DayOfWeek;
import java.time.LocalTime;

import br.ifsp.demo.exception.*;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.ifsp.demo.domain.model.*;
import br.ifsp.demo.domain.model.enums.RoomType;
import br.ifsp.demo.domain.repository.ActivityClassRepository;

@ExtendWith(MockitoExtension.class)
public class CreateActivityClassUseCaseTest {

    @InjectMocks
    private CreateActivityClassUseCase sut;

    @Mock
    private ActivityClassRepository activityClassRepo;

    private Trainer trainer;
    private Room room;
    private Sport sport;
    private Set<DayOfWeek> classDays;
    private Schedule schedule;
    private Schedule existingSchedule;

    @BeforeEach
    public void setup() {
        trainer = new Trainer(UUID.randomUUID(), "John Doe");
        room = new Room(UUID.randomUUID(), "Room A", RoomType.GYM, 10);
        sport = new Sport(UUID.randomUUID(), "Basketball", RoomType.GYM);

        classDays = Set.of(
                DayOfWeek.MONDAY,
                DayOfWeek.WEDNESDAY
        );

        schedule = new Schedule(
                classDays,
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        existingSchedule = schedule;
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should return a new ActivityClass when room, sport and trainer are available at the given period")

    public void shouldCreateClassWhenAvailable() {

        ActivityClass activityClass = new ActivityClass(
                room,
                sport,
                trainer,
                schedule,
                10,
                BigDecimal.valueOf(200)
        );

        when(activityClassRepo.save(any(ActivityClass.class)))
                .thenReturn(activityClass);

        ActivityClass result = sut.createNewActivityClass(
                room,
                sport,
                trainer,
                schedule,
                10,
                BigDecimal.valueOf(200)
        );



        assertThat(result).isEqualTo(activityClass);

        verify(activityClassRepo).save(any(ActivityClass.class));
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should not create ActivityClass when room is booked at the given period")

    public void shouldNotCreateActivityClassWhenRoomIsBooked() {

        Schedule newSchedule = new Schedule(
                classDays,
                LocalTime.of(10, 30),
                LocalTime.of(11, 30)
        );

        ActivityClass existingActivityClass = new ActivityClass(
                room,
                sport,
                trainer,
                existingSchedule,
                10,
                BigDecimal.valueOf(200)
        );

        when(activityClassRepo.findByRoom(room))
                .thenReturn(List.of(existingActivityClass));

        assertThatThrownBy(() ->
                sut.createNewActivityClass(
                        room,
                        sport,
                        trainer,
                        newSchedule,
                        10,
                        BigDecimal.valueOf(200)
                )
        ).isInstanceOf(RoomScheduleConflictException.class);


    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should not create ActivityClass when trainer is booked at the given period")

    public void shouldNotCreateActivityClassWhenTrainerIsBooked() {

        Schedule newSchedule = new Schedule(
                classDays,
                LocalTime.of(10, 30),
                LocalTime.of(11, 30)
        );

        ActivityClass existingActivityClass = new ActivityClass(
                room,
                sport,
                trainer,
                existingSchedule,
                10,
                BigDecimal.valueOf(200)
        );

        when(activityClassRepo.findByTrainer(trainer))
                .thenReturn(List.of(existingActivityClass));

        assertThatThrownBy(() ->
                sut.createNewActivityClass(
                        room,
                        sport,
                        trainer,
                        newSchedule,
                        10,
                        BigDecimal.valueOf(200)
                )
        ).isInstanceOf(TrainerScheduleConflictException.class);

  

    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should create a new ActivityClass when the schedule is adjacent to an existing class")

    public void shouldCreateClassWhenScheduleIsAdjacent() {

        ActivityClass existingActivityClass = new ActivityClass(
                room,
                sport,
                trainer,
                existingSchedule,
                10,
                BigDecimal.valueOf(200)
        );

        Schedule newSchedule = new Schedule(
                classDays,
                LocalTime.of(11, 0),
                LocalTime.of(12, 0)
        );

        ActivityClass activityClass = new ActivityClass(
                room,
                sport,
                trainer,
                newSchedule,
                10,
                BigDecimal.valueOf(200)
        );

        when(activityClassRepo.findByRoom(room))
                .thenReturn(List.of(existingActivityClass));

        when(activityClassRepo.save(any(ActivityClass.class)))
                .thenReturn(activityClass);

        ActivityClass result = sut.createNewActivityClass(
                room,
                sport,
                trainer,
                newSchedule,
                10,
                BigDecimal.valueOf(200)
        );



        assertThat(result).isEqualTo(activityClass);

        verify(activityClassRepo).save(any(ActivityClass.class));
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should not create a new ActivityClass when room type does not match sport type")
    void shouldNotCreateANewActivityClassWhenRoomTypeHasConflict() {

        RoomType sportType = RoomType.POOL;


        Sport sport = new Sport(
                UUID.randomUUID(),
                "Swimming",
                sportType
        );

        assertThatThrownBy(() ->
                sut.createNewActivityClass(
                        room,
                        sport,
                        trainer,
                        schedule,
                        10,
                        BigDecimal.valueOf(200)
                )
        ).isInstanceOf(RoomTypeConflictException.class);

    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("should not create ActivityClass when the provided capacity exceeds room capacity")
    void shouldNotCreateActivityClassWhenTheProvidedCapacityExceeds() {

        int differentCapacity = room.getCapacity() + 1;


        assertThatThrownBy(() ->
                sut.createNewActivityClass(
                        room,
                        sport,
                        trainer,
                        schedule,
                        differentCapacity,
                        BigDecimal.valueOf(200)
                )
        ).isInstanceOf(CapacityIsGreaterThanAcceptedException.class);

    }

    @Test
    @Tag("Functional")
    @Tag("UnitTest")
    @DisplayName("Should create ActivityClass when same trainer is available at a different period")
    void shouldCreateActivityClassWhenSameTrainerIsAvailableAtDifferentPeriod() {
        ActivityClass existingActivityClass = new ActivityClass(room, sport, trainer, existingSchedule, 10, new BigDecimal("250.00"));
        Schedule newSchedule = new Schedule(classDays, LocalTime.of(12, 0), LocalTime.of(13, 0));
        ActivityClass anotherActivityClass = new ActivityClass(room, sport, trainer, newSchedule, 10, new BigDecimal("250.00"));
        when(activityClassRepo.findByTrainer(trainer)).thenReturn(List.of(existingActivityClass));
        when(activityClassRepo.save(any(ActivityClass.class))).thenReturn(anotherActivityClass);
        ActivityClass result = sut.createNewActivityClass(room, sport, trainer, newSchedule, 10, new BigDecimal("250.00"));
        assertThat(result).isEqualTo(anotherActivityClass);
        verify(activityClassRepo).save(any(ActivityClass.class));
    }

    @Test
    @Tag("Functional")
    @Tag("UnitTest")
    @DisplayName("Should not create ActivityClass when room is null")
    void shouldNotCreateActivityClassWhenRoomIsNull() {
        assertThatThrownBy(() ->
                sut.createNewActivityClass(null, sport, trainer, schedule, 10, new BigDecimal("250.00")))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @Tag("Functional")
    @Tag("UnitTest")
    @DisplayName("Should not create ActivityClass when sport is null")
    void shouldNotCreateActivityClassWhenSportIsNull() {
        assertThatThrownBy(() ->
                sut.createNewActivityClass(room, null, trainer, schedule, 10, new BigDecimal("250.00")))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @Tag("Functional")
    @Tag("UnitTest")
    @DisplayName("Should not create ActivityClass when trainer is null")
    void shouldNotCreateActivityClassWhenTrainerIsNull() {
        assertThatThrownBy(() ->
                sut.createNewActivityClass(room, sport, null, schedule, 10, new BigDecimal("250.00")))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @Tag("Functional")
    @Tag("UnitTest")
    @DisplayName("Should not create ActivityClass when schedule is null")
    void shouldNotCreateActivityClassWhenScheduleIsNull() {
        assertThatThrownBy(() ->
                sut.createNewActivityClass(room, sport, trainer, null, 10, new BigDecimal("250.00")))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @Tag("Functional")
    @Tag("UnitTest")
    @DisplayName("Should not create ActivityClass when monthly fee is null")
    void shouldNotCreateActivityClassWhenMonthlyFeeIsNull() {
        assertThatThrownBy(() ->
                sut.createNewActivityClass(room, sport, trainer, schedule, 10, null))
                .isInstanceOf(NullPointerException.class);
    }
}