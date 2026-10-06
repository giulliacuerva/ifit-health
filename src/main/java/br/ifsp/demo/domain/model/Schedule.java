package br.ifsp.demo.domain.model;

import br.ifsp.demo.exception.InvalidScheduleException;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Set;

public class Schedule {

    private final Set<DayOfWeek> weekdays;
    private final LocalTime startTime;
    private final LocalTime endTime;

    private static final LocalTime MIN_TIME = LocalTime.of(6, 0);
    private static final LocalTime MAX_TIME = LocalTime.of(22, 0);

    public Schedule(Set<DayOfWeek> weekdays,LocalTime startTime,LocalTime endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new InvalidScheduleException("End time must be after start time");
        }
        if (startTime.isBefore(MIN_TIME) || endTime.isAfter(MAX_TIME)) {
            throw new InvalidScheduleException("Schedule must be between 06:00 and 22:00");
        }
        this.weekdays = weekdays;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean conflictsWith(Schedule otherSchedule) {
        boolean sameDay = weekdays.stream().anyMatch(otherSchedule.weekdays::contains);
        if (!sameDay) { return false; }

        return startTime.isBefore(otherSchedule.endTime) && otherSchedule.startTime.isBefore(endTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Schedule schedule = (Schedule) o;
        return Objects.equals(weekdays, schedule.weekdays) && Objects.equals(startTime, schedule.startTime) && Objects.equals(endTime, schedule.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(weekdays, startTime, endTime);
    }
}