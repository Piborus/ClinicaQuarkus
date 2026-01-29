package br.ce.clinica.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
public class FiliacaoRequest {

    @Schema(name = "Nome", description = "Nome da filiada", examples = { "Ana Clara" })
    @NotNull
    private String nome;

    @Schema(name = "Idade", description = "Idade da filiada", examples = { "25" })
    private Integer idade;

    @Schema(name = "Cpf", description = "CPF da filiada", examples = { "123.456.789-00" })
    private String cpf;

    @Schema(name = "Telefone", description = "Telefone da filiada", examples = { "(11) 91234-5678" })
    private String telefone;

    @Schema(name = "Email", description = "Email do filiado", examples = {"anaclara@email.com"})
    private String email;

    @Schema(name = "GrauDeParentesco", description = "Grau de parentesco", examples = {"FILHO"})
    private String grauDeParentesco;

}
