package br.ifsp.demo.domain.usecase;

import br.ifsp.demo.domain.model.*;
import br.ifsp.demo.domain.repository.ActivityClassRepository;
import br.ifsp.demo.exception.RoomScheduleConflictException;
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
        room = new Room(UUID.randomUUID(), "Room A");
        sport = new Sport(UUID.randomUUID(), "Basketball");
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
        assertThat(result.getRoom()).isEqualTo(room);
        assertThat(result.getSport()).isEqualTo(sport);
        assertThat(result.getTrainer()).isEqualTo(trainer);
        assertThat(result.getSchedule()).isEqualTo(schedule);

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
}