package br.ifsp.demo.domain.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;


public class ActivityClass {
    private final UUID id;
    private final Room room;
    private final Sport sport;
    private final Trainer trainer;
    private final Schedule schedule;
    private final int capacity;
    private final BigDecimal monthlyFee;

    public ActivityClass(Room room, Sport sport, Trainer trainer, Schedule schedule, int capacity, BigDecimal monthlyFee) {
        id = UUID.randomUUID();
        this.room = room;
        this.sport = sport;
        this.trainer = trainer;
        this.schedule = schedule;
        this.capacity = capacity;
        this.monthlyFee = monthlyFee;
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

    public int getCapacity() {
        return capacity;
    }

    public BigDecimal getMonthlyFee() {return monthlyFee;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityClass that = (ActivityClass) o;
        return Objects.equals(id, that.id)
                && Objects.equals(room, that.room)
                && Objects.equals(sport, that.sport)
                && Objects.equals(trainer, that.trainer)
                && Objects.equals(schedule, that.schedule)
                && capacity == that.capacity
                && Objects.equals(monthlyFee, that.monthlyFee);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, room, sport, trainer, schedule, capacity, monthlyFee);
    }
}
