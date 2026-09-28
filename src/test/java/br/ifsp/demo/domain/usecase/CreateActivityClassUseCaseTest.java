package br.ifsp.demo.domain.usecase;

import br.ifsp.demo.domain.model.*;
import br.ifsp.demo.domain.model.enums.RoomType;
import br.ifsp.demo.domain.repository.ActivityClassRepository;
import br.ifsp.demo.exception.RoomScheduleConflictException;
import br.ifsp.demo.exception.RoomTypeConflictException;
import br.ifsp.demo.exception.TrainerScheduleConflictException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class CreateActivityClassUseCaseTest {

    @InjectMocks
    private CreateActivityClassUseCase sut;
    @Mock
    private ActivityClassRepository activityClassRepo;

    private Trainer trainer;
    private Room room;
    private Sport sport;

    @BeforeEach
    public void setup() {
        trainer = new Trainer(UUID.randomUUID(), "John Doe");
        room = new Room(UUID.randomUUID(), "Room A", RoomType.GYM);
        sport = new Sport(UUID.randomUUID(), "Basketball", RoomType.GYM);
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should return a new ActivityClass when room, sport and trainer are available at the given period")
    public void shouldCreateClassWhenAvailable() {

        Set<DayOfWeek> classDays = Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);
        Schedule schedule = new Schedule(classDays,LocalTime.of(10, 0),LocalTime.of(11, 0));
        ActivityClass activityClass = new ActivityClass(room, sport, trainer, schedule);

        when(activityClassRepo.save(any(ActivityClass.class))).thenReturn(activityClass);

        ActivityClass result = sut.createNewActivityClass(room, sport, trainer, schedule);

        assertThat(result).isEqualTo(activityClass);

        verify(activityClassRepo).save(any(ActivityClass.class));
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should not create ActivityClass when room is booked at the given period")
    public void shouldNotCreateActivityClassWhenRoomIsBooked() {

        Set<DayOfWeek> classDays = Set.of(DayOfWeek.MONDAY);
        Schedule existingSchedule = new Schedule(classDays,LocalTime.of(10, 0),LocalTime.of(11, 0));
        Schedule newSchedule = new Schedule(classDays,LocalTime.of(10, 30),LocalTime.of(11, 30));

        ActivityClass existingActivityClass = new ActivityClass(room, sport, trainer, existingSchedule);

        when(activityClassRepo.findByRoom(room)).thenReturn(List.of(existingActivityClass));

        assertThatThrownBy(() -> sut.createNewActivityClass(room, sport, trainer, newSchedule))
                .isInstanceOf(RoomScheduleConflictException.class);

    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should not create ActivityClass when trainer is booked at the given period")
    public void shouldNotCreateActivityClassWhenTrainerIsBooked() {

        Set<DayOfWeek> classDays = Set.of(DayOfWeek.MONDAY);
        Schedule existingSchedule = new Schedule(classDays,LocalTime.of(10, 0),LocalTime.of(11, 0));
        Schedule newSchedule = new Schedule(classDays,LocalTime.of(10, 30),LocalTime.of(11, 30));

        ActivityClass existingActivityClass = new ActivityClass(room, sport, trainer, existingSchedule);

        when(activityClassRepo.findByTrainer(trainer)).thenReturn(List.of(existingActivityClass));

        assertThatThrownBy(() -> sut.createNewActivityClass(room, sport, trainer, newSchedule))
                .isInstanceOf(TrainerScheduleConflictException.class);

    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should create a new ActivityClass when the schedule is adjacent to an existing class")
    public void shouldCreateClassWhenScheduleIsAdjacent() {

        Set<DayOfWeek> classDays = Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);
        Schedule existingSchedule = new Schedule(classDays,LocalTime.of(10, 0),LocalTime.of(11, 0));
        ActivityClass existingActivityClass = new ActivityClass(room, sport, trainer, existingSchedule);

        Schedule newSchedule = new Schedule(classDays,LocalTime.of(11, 0),LocalTime.of(12, 0));
        ActivityClass activityClass = new ActivityClass(room, sport, trainer, newSchedule);

        when(activityClassRepo.findByRoom(room)).thenReturn(List.of(existingActivityClass));
        when(activityClassRepo.save(any(ActivityClass.class))).thenReturn(activityClass);

        ActivityClass result = sut.createNewActivityClass(room, sport, trainer, newSchedule);

        assertThat(result).isEqualTo(activityClass);

        verify(activityClassRepo).save(any(ActivityClass.class));
    }

    @Test
    @DisplayName("Should not create a new ActivityClass when room type does not match sport type")
    void shouldNotCreateANewActivityClassWhenRoomTypeHasConflict(){
        RoomType sportType = RoomType.POOL;
        Sport sport = new Sport(UUID.randomUUID(), "Swimming", sportType);

        Set<DayOfWeek> classDays = Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);
        Schedule schedule = new Schedule(classDays,LocalTime.of(10, 0),LocalTime.of(11, 0));

        assertThatThrownBy(() -> sut.createNewActivityClass(room, sport, trainer, schedule))
                .isInstanceOf(RoomTypeConflictException.class);
    }
}