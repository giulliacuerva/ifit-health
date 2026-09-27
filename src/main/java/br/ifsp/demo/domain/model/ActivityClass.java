package br.ifsp.demo.domain.model;

import java.util.Objects;
import java.util.UUID;


public class ActivityClass {
    private final UUID id;
    private final Room room;
    private final Sport sport;
    private final Trainer trainer;
    private final Schedule schedule;

    public ActivityClass(Room room, Sport sport, Trainer trainer, Schedule schedule) {
        id = UUID.randomUUID();
        this.room = room;
        this.sport = sport;
        this.trainer = trainer;
        this.schedule = schedule;
    }

    public UUID getId() {
        return id;
    }

    public Room getRoom() {
        return room;
    }

    public Sport getSport() {
        return sport;
    }

    public Trainer getTrainer() {
        return trainer;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityClass that = (ActivityClass) o;
        return Objects.equals(id, that.id)
                && Objects.equals(room, that.room)
                && Objects.equals(sport, that.sport)
                && Objects.equals(trainer, that.trainer)
                && Objects.equals(schedule, that.schedule);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, room, sport, trainer, schedule);
    }
}
