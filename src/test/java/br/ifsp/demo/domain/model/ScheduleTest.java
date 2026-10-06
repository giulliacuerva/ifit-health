package br.ifsp.demo.domain.model;

import java.util.*;
import java.time.DayOfWeek;
import java.time.LocalTime;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.ifsp.demo.exception.*;


@ExtendWith(MockitoExtension.class)
public class ScheduleTest {
    private Set<DayOfWeek> classDays;

    @BeforeEach
    void setup() {
        classDays = Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("should not create a schedule with end time before start time")
    void shouldNotCreateAScheduleWithEndTimeBeforeStartTime(){
        assertThatThrownBy(() -> new Schedule(classDays,LocalTime.of(11, 0),LocalTime.of(10, 0)))
                .isInstanceOf(InvalidScheduleException.class);
    }

    @Test
    @Tag("Functional")
    @Tag("UnitTest")
    @DisplayName("Should not create Schedule when start and end time are equal")
    void shouldNotCreateScheduleWhenStartAndEndTimeAreEqual() {
        assertThatThrownBy(() ->
                new Schedule(classDays, LocalTime.of(10, 0), LocalTime.of(10, 0)))
                .isInstanceOf(InvalidScheduleException.class);
    }
}
