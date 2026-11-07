package br.ifsp.demo.controller.dto;

import java.util.UUID;

public record TrainerCatalogResponse(
        UUID id,
        String name,
        boolean active
) {
}
