package br.ifsp.demo.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

@Schema(description = "Fields required to create or update an activity class.")
public record ActivityClassRequest(
        @Schema(description = "Sport UUID.", requiredMode = Schema.RequiredMode.REQUIRED,
                example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull UUID sportId,
        @Schema(description = "Room UUID.", requiredMode = Schema.RequiredMode.REQUIRED,
                example = "550e8400-e29b-41d4-a716-446655440001")
        @NotNull UUID roomId,
        @Schema(description = "Trainer UUID.", requiredMode = Schema.RequiredMode.REQUIRED,
                example = "550e8400-e29b-41d4-a716-446655440002")
        @NotNull UUID trainerId,
        @Schema(description = "Weekdays when the class takes place.", requiredMode = Schema.RequiredMode.REQUIRED,
                example = "[\"MONDAY\", \"WEDNESDAY\"]")
        @NotEmpty Set<DayOfWeek> weekdays,
        @Schema(description = "Class start time (HH:mm:ss).", requiredMode = Schema.RequiredMode.REQUIRED,
                example = "08:00:00")
        @NotNull LocalTime startTime,
        @Schema(description = "Class end time (HH:mm:ss).", requiredMode = Schema.RequiredMode.REQUIRED,
                example = "09:00:00")
        @NotNull LocalTime endTime,
        @Schema(description = "Maximum number of students.", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED,
                example = "20")
        @Positive int capacity,
        @Schema(description = "Monthly fee in BRL.", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED,
                example = "150.00")
        @NotNull @DecimalMin("0.0") BigDecimal monthlyFee
) {
}
