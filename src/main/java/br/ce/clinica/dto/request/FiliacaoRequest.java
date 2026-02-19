package br.ce.clinica.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.validator.constraints.br.CPF;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FiliacaoRequest {

    @Schema(name = "nome", description = "Nome da filiada", examples = { "Ana Clara" })
    @NotNull
    private String nome;

    @Schema(name = "idade", description = "Idade da filiada", examples = { "25" })
    private Integer idade;

    @Schema(name = "cpf", description = "CPF da filiada", examples = { "123.456.789-00" })
    @CPF
    private String cpf;

    @Schema(name = "telefone", description = "Telefone da filiada", examples = { "(11) 91234-5678" })
    private String telefone;

    @Schema(name = "email", description = "Email do filiado", examples = {"anaclara@email.com"})
    private String email;

    @Schema(name = "grauDeParentesco", description = "Grau de parentesco", examples = {"FILHO"})
    private String grauDeParentesco;

}
