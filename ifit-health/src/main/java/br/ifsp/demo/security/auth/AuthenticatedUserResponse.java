package br.ifsp.demo.security.auth;

import br.ifsp.demo.security.user.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Informações do usuário autenticado.")
public record AuthenticatedUserResponse(

        @Schema(description = "Identificador da conta do usuário.",
                example = "550e8400-e29b-41d4-a716-446655440000")
        UUID userId,

        @Schema(description = "Identificador do cadastro do aluno associado à conta.",
                example = "550e8400-e29b-41d4-a716-446655440001", nullable = true)
        UUID customerId,

        @Schema(description = "Nome do usuário.", example = "Márcio")
        String name,

        @Schema(description = "Sobrenome do usuário.", example = "Dantas")
        String lastname,

        @Schema(description = "E-mail utilizado para autenticação.",
                example = "marcio@email.com")
        String email,

        @Schema(description = "Perfil de autorização do usuário.",
                example = "USER")
        Role role
) {
}