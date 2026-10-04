package br.ifsp.demo.domain.usecase;

import br.ifsp.demo.domain.model.*;
import br.ifsp.demo.domain.repository.ActivityClassRepository;
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

    @InjectMocks
    private EditActivityClassUseCase sut;

    private Trainer trainer;
    private Room room;
    private Sport sport;
    private Schedule schedule;
    private ActivityClass activityClass;

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
}