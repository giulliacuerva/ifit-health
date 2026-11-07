package br.ifsp.demo.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

@Schema(description = "Activity class information enriched with catalog names and current availability.")
public record ActivityClassAvailabilityResponse(
        @Schema(description = "Activity class UUID.", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
        @Schema(description = "Sport UUID.")
        UUID sportId,
        @Schema(description = "Room UUID.")
        UUID roomId,
        @Schema(description = "Trainer UUID.")
        UUID trainerId,
        @Schema(description = "Days of the week when the class takes place.", example = "[\"MONDAY\", \"WEDNESDAY\"]")
        Set<DayOfWeek> weekdays,
        @Schema(description = "Start time.", example = "08:00:00")
        LocalTime startTime,
        @Schema(description = "End time.", example = "09:00:00")
        LocalTime endTime,
        @Schema(description = "Maximum number of students.", example = "20")
        int capacity,
        @Schema(description = "Monthly fee currently in effect, in BRL.", example = "150.00")
        BigDecimal monthlyFee,
        @Schema(description = "Whether the activity is active.", example = "true")
        boolean active,
        @Schema(description = "Sport display name.", example = "Natação")
        String sportName,
        @Schema(description = "Room display name.", example = "Piscina")
        String roomName,
        @Schema(description = "Trainer display name.", example = "Maria Silva")
        String trainerName,
        @Schema(description = "Number of students currently enrolled.", example = "12")
        int enrolledCount,
        @Schema(description = "Number of available spots. Inactive activities return zero.", example = "8")
        int availableSpots
) {
}
