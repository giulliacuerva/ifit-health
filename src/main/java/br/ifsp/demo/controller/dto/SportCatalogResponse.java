package br.ifsp.demo.controller.dto;

import br.ifsp.demo.domain.model.enums.RoomType;

import java.util.UUID;

public record SportCatalogResponse(
        UUID id,
        String name,
        String description,
        RoomType roomType,
        boolean active
) {
}
