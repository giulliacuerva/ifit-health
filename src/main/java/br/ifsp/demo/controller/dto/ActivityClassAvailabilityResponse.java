package br.ifsp.demo.controller.dto;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

public record ActivityClassAvailabilityResponse(
        UUID id,
        UUID sportId,
        UUID roomId,
        UUID trainerId,
        Set<DayOfWeek> weekdays,
        LocalTime startTime,
        LocalTime endTime,
        int capacity,
        BigDecimal monthlyFee,
        boolean active,
        String sportName,
        String roomName,
        String trainerName,
        int enrolledCount,
        int availableSpots
) {
}
