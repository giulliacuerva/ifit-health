package br.ifsp.demo.domain.model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Set;

public class Schedule {

    private final Set<DayOfWeek> weekdays;
    private final LocalTime startTime;
    private final LocalTime endTime;

    public Schedule(Set<DayOfWeek> weekdays,LocalTime startTime,LocalTime endTime) {
        this.weekdays = weekdays;
        this.startTime = startTime;
        this.endTime = endTime;
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