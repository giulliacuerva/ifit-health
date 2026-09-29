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
    @Test
    @DisplayName("should not create a schedule with end time before start time")
    void shouldNotCreateAScheduleWithEndTimeBeforeStartTime(){
        Set<DayOfWeek> classDays = Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);

        assertThatThrownBy(() -> new Schedule(classDays,LocalTime.of(11, 0),LocalTime.of(10, 0)))
                .isInstanceOf(InvalidScheduleException.class);
    }
}
