package br.ifsp.demo.domain.model;

import br.ifsp.demo.domain.model.enums.RoomType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ActivityClassTest {

    private final UUID id = UUID.randomUUID();
    private final Room room = new Room(
            UUID.randomUUID(), "Room A", RoomType.GYM, 20
    );
    private final Sport sport = new Sport(
            UUID.randomUUID(), "Basketball", RoomType.GYM
    );
    private final Trainer trainer = new Trainer(UUID.randomUUID(), "Ana");
    private final Schedule schedule = new Schedule(
            Set.of(DayOfWeek.MONDAY),
            LocalTime.of(10, 0),
            LocalTime.of(11, 0)
    );

    private ActivityClass createActivityClass(UUID id, Room room, Sport sport, Trainer trainer, Schedule schedule, int capacity, BigDecimal monthlyFee) {
        return new ActivityClass(
                id, room, sport, trainer, schedule, capacity, monthlyFee, true
        );
    }

    private ActivityClass createDefaultActivityClass() {
        return createActivityClass(
                id, room, sport, trainer, schedule, 10, new BigDecimal("180.00")
        );
    }

    @Test
    @Tag("Structural")
    @Tag("UnitTest")
    @DisplayName("Should consider the same activity class equal")
    void shouldConsiderTheSameActivityClassEqual() {
        ActivityClass activity = createDefaultActivityClass();

        assertThat(activity.equals(activity)).isTrue();
    }

    @Test
    @Tag("Structural")
    @Tag("UnitTest")
    @DisplayName("Should return false when compared with null")
    void shouldReturnFalseWhenComparedWithNull() {
        ActivityClass activity = createDefaultActivityClass();

        assertThat(activity.equals(null)).isFalse();
    }

    @Test
    @Tag("Structural")
    @Tag("UnitTest")
    @DisplayName("Should return false when compared with another type")
    void shouldReturnFalseWhenComparedWithAnotherType() {
        ActivityClass activity = createDefaultActivityClass();

        assertThat(activity.equals("not an activity class")).isFalse();
    }

    @Test
    @Tag("Structural")
    @Tag("UnitTest")
    @DisplayName("Should consider activity classes with the same data equal")
    void shouldConsiderActivityClassesWithTheSameDataEqual() {
        ActivityClass first = createDefaultActivityClass();
        ActivityClass second = createDefaultActivityClass();

        assertThat(first.equals(second)).isTrue();
    }

    @Test
    @Tag("Structural")
    @Tag("UnitTest")
    @DisplayName("Should return false when the id differs")
    void shouldReturnFalseWhenIdDiffers() {
        ActivityClass first = createDefaultActivityClass();
        ActivityClass second = createActivityClass(
                UUID.randomUUID(), room, sport, trainer, schedule,
                10, new BigDecimal("180.00")
        );

        assertThat(first.equals(second)).isFalse();
    }

    @Test
    @Tag("Structural")
    @Tag("UnitTest")
    @DisplayName("Should return false when the room differs")
    void shouldReturnFalseWhenRoomDiffers() {
        ActivityClass first = createDefaultActivityClass();
        Room anotherRoom = new Room(
                UUID.randomUUID(), "Room B", RoomType.GYM, 20
        );
        ActivityClass second = createActivityClass(
                id, anotherRoom, sport, trainer, schedule,
                10, new BigDecimal("180.00")
        );

        assertThat(first.equals(second)).isFalse();
    }


}