package br.ifsp.demo.controller.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

public record ActivityClassRequest(
        @NotNull UUID sportId,
        @NotNull UUID roomId,
        @NotNull UUID trainerId,
        @NotEmpty Set<DayOfWeek> weekdays,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        @Positive int capacity,
        @NotNull @DecimalMin("0.0") BigDecimal monthlyFee
) {}
