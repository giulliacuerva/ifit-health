package br.ifsp.demo.controller.dto;

import br.ifsp.demo.domain.model.enums.RoomType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Room available in the activity catalog.")
public record RoomCatalogResponse(
        @Schema(description = "Room UUID.")
        UUID id,
        @Schema(description = "Room display name.", example = "Piscina")
        String name,
        @Schema(description = "Room type.", example = "POOL")
        RoomType roomType,
        @Schema(description = "Maximum room capacity.", example = "20")
        int capacity
) {
}
