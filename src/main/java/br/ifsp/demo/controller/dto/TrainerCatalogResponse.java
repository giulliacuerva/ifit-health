package br.ifsp.demo.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Trainer available in the activity catalog.")
public record TrainerCatalogResponse(
        @Schema(description = "Trainer UUID.")
        UUID id,
        @Schema(description = "Trainer full name.", example = "Maria Silva")
        String name,
        @Schema(description = "Whether the trainer is active.", example = "true")
        boolean active
) {
}
