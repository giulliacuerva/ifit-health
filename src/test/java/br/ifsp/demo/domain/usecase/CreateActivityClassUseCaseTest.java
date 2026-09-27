package br.ifsp.demo.domain.usecase;

import br.ifsp.demo.domain.model.*;
import br.ifsp.demo.domain.repository.ActivityClassRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class CreateActivityClassUseCaseTest {

    @InjectMocks
    private CreateActivityClassUseCase sut;
    @Mock
    private ActivityClassRepository activityClassRepo;

    private final UUID roomId = UUID.randomUUID();
    private final UUID sportId = UUID.randomUUID();
    private final UUID trainerId = UUID.randomUUID();

    @Test
    @DisplayName("Should return a new ActivityClass when room, sport and trainer are available at the given period")
    public void shouldCreateClassWhenAvailable() {

        Trainer trainer = new Trainer(trainerId, "John Doe");
        Room room = new Room(roomId, "Room A");
        Sport sport = new Sport(sportId, "Basketball");
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
}