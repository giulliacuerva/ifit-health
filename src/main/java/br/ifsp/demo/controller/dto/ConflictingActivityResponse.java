package br.ifsp.demo.controller.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

public record ConflictingActivityResponse(
        UUID activityClassId,
        String sportName,
        Set<DayOfWeek> weekdays,
        LocalTime startTime,
        LocalTime endTime
) {
}
