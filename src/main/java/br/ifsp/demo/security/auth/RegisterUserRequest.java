package br.ifsp.demo.security.auth;

import br.ifsp.demo.domain.model.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Dados para criar uma conta de aluno.")
public record RegisterUserRequest(
        @Schema(description = "Nome", example = "Ana")
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        String name,

        @Schema(description = "Sobrenome", example = "Souza")
        @NotBlank(message = "O sobrenome é obrigatório.")
        @Size(max = 100, message = "O sobrenome deve ter no máximo 100 caracteres.")
        String lastname,

        @Schema(description = "E-mail de acesso", example = "ana.souza@example.com")
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Informe um e-mail válido.")
        @Size(max = 255, message = "O e-mail deve ter no máximo 255 caracteres.")
        String email,

        @Schema(description = "Senha com no mínimo 8 caracteres", example = "Senha@123")
        @NotBlank(message = "A senha é obrigatória.")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres.")
        String password,
        @Schema(description = "CPF com ou sem pontuação", example = "123.456.789-09")
        @NotBlank(message = "O CPF é obrigatório.")
        @Pattern(regexp = "^(\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2})$",
                message = "Informe o CPF com 11 números, com ou sem pontuação.")
        String cpf,

        @Schema(description = "Data de nascimento no formato yyyy-MM-dd", example = "1998-05-12")
        @NotNull(message = "A data de nascimento é obrigatória.")
        @Past(message = "A data de nascimento deve estar no passado.")
        LocalDate birthdate,

        @Schema(description = "Gênero: MALE ou FEMALE", example = "FEMALE")
        @NotNull(message = "O gênero é obrigatório.")
        Gender gender,

        @Schema(description = "Telefone opcional", example = "16999990000")
        @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres.")
        String tel,

        @Schema(description = "Endereço opcional. Se informado, todos os campos são obrigatórios.")
        @Valid
        AddressRequest address
) {
        public record AddressRequest(
                @NotBlank(message = "O CEP é obrigatório.")
                @Size(max = 10, message = "O CEP deve ter no máximo 10 caracteres.")
                @Schema(example = "13560-001")
                String cep,

                @NotBlank(message = "O número do endereço é obrigatório.")
                @Size(max = 20, message = "O número deve ter no máximo 20 caracteres.")
                @Schema(example = "100")
                String number,

                @NotBlank(message = "A rua é obrigatória.")
                @Size(max = 150, message = "A rua deve ter no máximo 150 caracteres.")
                @Schema(example = "Rua das Flores")
                String street,

                @NotBlank(message = "A cidade é obrigatória.")
                @Size(max = 100, message = "A cidade deve ter no máximo 100 caracteres.")
                @Schema(example = "São Carlos")
                String city,

                @NotBlank(message = "O estado é obrigatório.")
                @Size(min = 2, max = 2, message = "Informe a sigla do estado com 2 letras.")
                @Schema(example = "SP")
                String state
        ) {
        }
}
