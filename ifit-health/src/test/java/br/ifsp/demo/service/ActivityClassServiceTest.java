package br.ifsp.demo.service;

import br.ifsp.demo.domain.model.ActivityClass;
import br.ifsp.demo.domain.model.Room;
import br.ifsp.demo.domain.model.Schedule;
import br.ifsp.demo.domain.model.Sport;
import br.ifsp.demo.domain.model.Trainer;
import br.ifsp.demo.domain.repository.ActivityClassRepository;
import br.ifsp.demo.domain.usecase.CreateActivityClassUseCase;
import br.ifsp.demo.domain.usecase.EditActivityClassUseCase;
import br.ifsp.demo.exception.ResourceNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityClassServiceTest {

    @Mock
    private ActivityClassRepository repository;

    @Mock
    private CreateActivityClassUseCase createUseCase;

    @Mock
    private EditActivityClassUseCase editUseCase;

    @InjectMocks
    private ActivityClassService service;

    private UUID activityId;
    private UUID roomId;
    private UUID sportId;
    private UUID trainerId;

    private Room room;
    private Sport sport;
    private Trainer trainer;
    private ActivityClass activityClass;

    private final Set<DayOfWeek> weekdays = Set.of(DayOfWeek.MONDAY);
    private final LocalTime startTime = LocalTime.of(10, 0);
    private final LocalTime endTime = LocalTime.of(11, 0);
    private final BigDecimal monthlyFee = new BigDecimal("150.00");
    private final int capacity = 20;

    @BeforeEach
    void setUp() {activityId = UUID.randomUUID();
        roomId = UUID.randomUUID();
        sportId = UUID.randomUUID();
        trainerId = UUID.randomUUID();

        room = mock(Room.class);
        sport = mock(Sport.class);
        trainer = mock(Trainer.class);
        activityClass = mock(ActivityClass.class);
    }


    @Test
    void shouldReturnAllActivityClasses() {
        Collection<ActivityClass> expected = List.of(activityClass, mock(ActivityClass.class));

        when(repository.findAll()).thenReturn(expected);

        Collection<ActivityClass> result = service.findAll();

        assertThat(result).isEqualTo(expected);
        verify(repository).findAll();
    }


    @Test
    void shouldReturnActivityClassWhenFound() {
        when(repository.findById(activityId)).thenReturn(activityClass);

        ActivityClass result = service.findById(activityId);

        assertThat(result).isEqualTo(activityClass);
        verify(repository).findById(activityId);
    }

    @Test
    void shouldThrowExceptionWhenActivityClassNotFound() {
        when(repository.findById(activityId)).thenReturn(null);

        assertThatThrownBy(() -> service.findById(activityId))
                .isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("Activity class not found");

        verify(repository).findById(activityId);
    }


    @Test
    void shouldCreateActivityClassWhenAllResourcesAreFound() {
        when(repository.findRoomById(roomId)).thenReturn(room);
        when(repository.findSportById(sportId)).thenReturn(sport);
        when(repository.findTrainerById(trainerId)).thenReturn(trainer);

        when(createUseCase.createNewActivityClass(eq(room), eq(sport), eq(trainer), any(Schedule.class), eq(capacity), eq(monthlyFee))).thenReturn(activityClass);

        ActivityClass result = service.create(roomId, sportId, trainerId, weekdays, startTime, endTime, capacity, monthlyFee);

        assertThat(result).isEqualTo(activityClass);

        verify(repository).findRoomById(roomId);
        verify(repository).findSportById(sportId);
        verify(repository).findTrainerById(trainerId);

        verify(createUseCase).createNewActivityClass(eq(room), eq(sport), eq(trainer), any(Schedule.class), eq(capacity), eq(monthlyFee));
    }

    @Test
    void shouldThrowExceptionWhenRoomNotFoundDuringCreation() {
        when(repository.findRoomById(roomId)).thenReturn(null);

        assertThatThrownBy(() -> service.create(roomId, sportId, trainerId, weekdays, startTime, endTime, capacity, monthlyFee))
                .isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("Room not found");

        verify(repository, never()).findSportById(any());
        verify(repository, never()).findTrainerById(any());
        verifyNoInteractions(createUseCase);
    }

    @Test
    void shouldThrowExceptionWhenSportNotFoundDuringCreation() {
        when(repository.findRoomById(roomId)).thenReturn(room);
        when(repository.findSportById(sportId)).thenReturn(null);

        assertThatThrownBy(() -> service.create(roomId, sportId, trainerId, weekdays, startTime, endTime, capacity, monthlyFee))
                .isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("Sport not found");

        verify(repository, never()).findTrainerById(any());
        verifyNoInteractions(createUseCase);
    }

    @Test
    void shouldThrowExceptionWhenTrainerNotFoundDuringCreation() {
        when(repository.findRoomById(roomId)).thenReturn(room);
        when(repository.findSportById(sportId)).thenReturn(sport);
        when(repository.findTrainerById(trainerId)).thenReturn(null);

        assertThatThrownBy(() -> service.create(roomId, sportId, trainerId, weekdays, startTime, endTime, capacity, monthlyFee))
                .isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("Trainer not found");

        verifyNoInteractions(createUseCase);
    }

    // EDIT

    @Test
    void shouldEditActivityClassWhenAllResourcesAreFound() {
        when(repository.findById(activityId)).thenReturn(activityClass);
        when(repository.findRoomById(roomId)).thenReturn(room);
        when(repository.findSportById(sportId)).thenReturn(sport);
        when(repository.findTrainerById(trainerId)).thenReturn(trainer);

        when(editUseCase.edit(eq(activityId), eq(room), eq(sport), eq(trainer), any(Schedule.class), eq(capacity), eq(monthlyFee))).thenReturn(activityClass);

        ActivityClass result = service.edit(
                activityId, roomId, sportId, trainerId, weekdays, startTime, endTime, capacity, monthlyFee);

        assertThat(result).isEqualTo(activityClass);

        verify(repository).findById(activityId);
        verify(repository).findRoomById(roomId);
        verify(repository).findSportById(sportId);
        verify(repository).findTrainerById(trainerId);

        verify(editUseCase).edit(eq(activityId), eq(room), eq(sport), eq(trainer), any(Schedule.class), eq(capacity), eq(monthlyFee)
        );
    }

    @Test
    void shouldThrowExceptionWhenActivityClassNotFoundDuringEdit() {
        when(repository.findById(activityId)).thenReturn(null);

        assertThatThrownBy(() -> service.edit(activityId, roomId, sportId, trainerId, weekdays, startTime, endTime, capacity, monthlyFee))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Activity class not found");

        verify(repository, never()).findRoomById(any());
        verify(repository, never()).findSportById(any());
        verify(repository, never()).findTrainerById(any());
        verifyNoInteractions(editUseCase);
    }

    @Test
    void shouldThrowExceptionWhenRoomNotFoundDuringEdit() {
        when(repository.findById(activityId)).thenReturn(activityClass);
        when(repository.findRoomById(roomId)).thenReturn(null);

        assertThatThrownBy(() -> service.edit(activityId, roomId, sportId, trainerId, weekdays, startTime, endTime, capacity, monthlyFee))
                .isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("Room not found");

        verifyNoInteractions(editUseCase);
    }

    @Test
    void shouldThrowExceptionWhenSportNotFoundDuringEdit() {
        when(repository.findById(activityId)).thenReturn(activityClass);
        when(repository.findRoomById(roomId)).thenReturn(room);
        when(repository.findSportById(sportId)).thenReturn(null);

        assertThatThrownBy(() -> service.edit(activityId, roomId, sportId, trainerId, weekdays, startTime, endTime, capacity, monthlyFee))
                .isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("Sport not found");

        verify(repository, never()).findTrainerById(any());
        verifyNoInteractions(editUseCase);
    }

    @Test
    void shouldThrowExceptionWhenTrainerNotFoundDuringEdit() {
        when(repository.findById(activityId)).thenReturn(activityClass);
        when(repository.findRoomById(roomId)).thenReturn(room);
        when(repository.findSportById(sportId)).thenReturn(sport);
        when(repository.findTrainerById(trainerId)).thenReturn(null);

        assertThatThrownBy(() -> service.edit(activityId, roomId, sportId, trainerId, weekdays,
                startTime, endTime, capacity, monthlyFee)).isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("Trainer not found");

        verifyNoInteractions(editUseCase);
    }
}