package br.ifsp.demo.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

@Schema(description = "Existing activity schedule that conflicts with the proposed update.")
public record ConflictingActivityResponse(
        @Schema(description = "Conflicting activity class UUID.")
        UUID activityClassId,
        @Schema(description = "Sport name.", example = "Natação")
        String sportName,
        @Schema(description = "Weekdays of the conflicting activity.", example = "[\"MONDAY\", \"WEDNESDAY\"]")
        Set<DayOfWeek> weekdays,
        @Schema(description = "Start time.", example = "08:00:00")
        LocalTime startTime,
        @Schema(description = "End time.", example = "09:00:00")
        LocalTime endTime
) {
}
