package br.ifsp.demo.security.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record RegisterUserResponse(
        @Schema(description = "Identificador da conta de autenticação.")
        UUID userId,
        @Schema(description = "Identificador do aluno na tabela customers.")
        UUID customerId
) {
}
