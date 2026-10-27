package br.ifsp.demo.controller.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record EnrollmentRequest(@NotEmpty List<UUID> activityClassIds) {}
