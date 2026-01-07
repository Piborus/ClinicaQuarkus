package br.ce.clinica.dto.request;

import br.ce.clinica.enums.Sexo;
import lombok.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PacienteRequest {

    @Schema(name = "nome", description = "Nome do paciente", examples = {"João da Silva"})
    @NonNull
    private String nome;

    @Schema(name = "idade", description = "Idade do paciente", examples = {"30"} )
    private Integer idade;

    @Schema(name = "sexo", description = "Sexo do paciente", examples = {"MASCULINO"})
    private Sexo sexo;

    @Schema(name = "dataNascimento", description = "Data de nascimento do paciente", examples = {"1990-01-01"}, format = "date")
    @NonNull
    private LocalDate dataNascimento;

    @Schema(name = "cpf", description = "CPF do paciente", examples = {"123.456.789-00"})
    private String cpf;

    @Schema(name = "rg", description = "RG do paciente", examples = {"200312312334"})
    private String rg;

    @Schema(name = "telefone", description = "Telefone do paciente", examples = {"(11) 91234-5678"})
    private String telefone;

    @Schema(name = "email", description = "Email do paciente", examples = {"jj@gmail.com"})
    private String email;

    private EnderecoRequest endereco;
}

