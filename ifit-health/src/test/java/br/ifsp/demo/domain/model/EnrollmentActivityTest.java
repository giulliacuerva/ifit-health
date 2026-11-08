package br.ifsp.demo.domain.model;

import br.ifsp.demo.domain.model.enums.RoomType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EnrollmentActivityTest {

    @Test
    void shouldCreateEnrollmentActivityWithGivenStartDate() {
        Schedule schedule = new Schedule(
                Set.of(DayOfWeek.MONDAY),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        Room room = new Room(
                UUID.randomUUID(),
                "Room A",
                RoomType.GYM,
                10
        );

        Sport sport = new Sport(
                UUID.randomUUID(),
                "Basketball",
                RoomType.GYM
        );

        Trainer trainer = new Trainer(
                UUID.randomUUID(),
                "John Doe"
        );

        ActivityClass activityClass = new ActivityClass(
                room,
                sport,
                trainer,
                schedule,
                10,
                new BigDecimal("180.00")
        );

        LocalDate startDate = LocalDate.of(2026, 10, 1);

        EnrollmentActivity enrollmentActivity =
                new EnrollmentActivity(
                        activityClass,
                        new BigDecimal("180.00"),
                        startDate
                );

        assertThat(enrollmentActivity.getStartDate())
                .isEqualTo(startDate);
    }
}