package br.ce.clinica.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioRequest {

    @NotBlank
    @Schema(description = "Nome do usuário", examples = {"João da Silva"})
    private String nome;

    @Email
    @NotBlank
    @Schema(description = "Email do usuário", examples =  {"jda@email.com"})
    private String email;

    @NotBlank
    @Schema(description = "Senha do usuário", examples = {"senha123"})
    private String senha;
}
