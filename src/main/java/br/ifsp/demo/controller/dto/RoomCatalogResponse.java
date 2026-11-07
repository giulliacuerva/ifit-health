package br.ifsp.demo.controller.dto;

import br.ifsp.demo.domain.model.enums.RoomType;

import java.util.UUID;

public record RoomCatalogResponse(
        UUID id,
        String name,
        RoomType roomType,
        int capacity
) {
}
