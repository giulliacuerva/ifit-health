package br.ifsp.demo.controller.dto;

import br.ifsp.demo.domain.model.enums.RoomType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Sport available in the activity catalog.")
public record SportCatalogResponse(
        @Schema(description = "Sport UUID.")
        UUID id,
        @Schema(description = "Sport name.", example = "Natação")
        String name,
        @Schema(description = "Sport description.", example = "Aulas e treinamentos de natação")
        String description,
        @Schema(description = "Type of room required by the sport.", example = "POOL")
        RoomType roomType,
        @Schema(description = "Whether the sport is active.", example = "true")
        boolean active
) {
}
