package br.ifsp.demo.controller.dto;

import br.ifsp.demo.domain.model.ActivityClass;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

public record ActivityClassResponse(
        UUID id,
        UUID sportId,
        UUID roomId,
        UUID trainerId,
        Set<DayOfWeek> weekdays,
        LocalTime startTime,
        LocalTime endTime,
        int capacity,
        BigDecimal monthlyFee,
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
