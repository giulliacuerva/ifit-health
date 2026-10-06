package br.ifsp.demo.domain.model;

import java.util.*;
import java.time.DayOfWeek;
import java.time.LocalTime;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
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

    @ParameterizedTest
    @Tag("Functional")
    @Tag("UnitTest")
    @CsvSource({
            "06:00, 07:00",
            "06:01, 07:00",
            "21:00, 21:59",
            "21:00, 22:00"
    })
    @DisplayName("Should create Schedule when time is within allowed period")
    void shouldCreateScheduleWhenTimeIsWithinAllowedPeriod(String start, String end) {
        Schedule schedule = new Schedule(classDays, LocalTime.parse(start), LocalTime.parse(end));
        assertThat(schedule).isNotNull();
    }

    @ParameterizedTest
    @Tag("Functional")
    @Tag("UnitTest")
    @CsvSource({
            "05:59, 07:00",
            "21:00, 22:01",
    })
    @DisplayName("Should not create Schedule when time is invalid")
    void shouldNotCreateScheduleWhenTimeIsInvalid(String start, String end) {
        assertThatThrownBy(() -> new Schedule(classDays, LocalTime.parse(start), LocalTime.parse(end)))
                .isInstanceOf(InvalidScheduleException.class);
    }
}
