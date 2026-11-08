package br.ifsp.demo.controller.dto;

import br.ifsp.demo.domain.model.ActivityClass;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

@Schema(description = "Activity class data returned after creation, update or detail lookup.")
public record ActivityClassResponse(
        @Schema(description = "Activity class UUID.")
        UUID id,
        @Schema(description = "Sport UUID.")
        UUID sportId,
        @Schema(description = "Room UUID.")
        UUID roomId,
        @Schema(description = "Trainer UUID.")
        UUID trainerId,
        @Schema(description = "Weekdays when the class takes place.")
        Set<DayOfWeek> weekdays,
        @Schema(description = "Start time.", example = "08:00:00")
        LocalTime startTime,
        @Schema(description = "End time.", example = "09:00:00")
        LocalTime endTime,
        @Schema(description = "Maximum number of students.", example = "20")
        int capacity,
        @Schema(description = "Monthly fee currently returned by the activity domain model, in BRL.", example = "150.00")
        BigDecimal monthlyFee,
        @Schema(description = "Whether the activity is active.", example = "true")
        boolean active
) {
    public static ActivityClassResponse from(ActivityClass activity) {
        return new ActivityClassResponse(activity.getId(), activity.getSport().getId(),
                activity.getRoom().getId(), activity.getTrainer().getId(),
                activity.getSchedule().getWeekdays(), activity.getSchedule().getStartTime(),
                activity.getSchedule().getEndTime(), activity.getCapacity(),
                activity.getMonthlyFee(), activity.isActive());
    }
}
