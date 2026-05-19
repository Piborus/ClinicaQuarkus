package br.ce.clinica.dto.request;

import br.ce.clinica.enums.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PacienteRequest {

    @Schema(name = "nome", description = "Nome do paciente", examples = {"João da Silva"})
    @NotNull
    private String nome;

    @Schema(name = "idade", description = "Idade do paciente", examples = {"30"} )
    private Integer idade;

    @Schema(name = "sexo", description = "Sexo do paciente", examples = {"MASCULINO"})
    private Sexo sexo;

    @Schema(name = "dataNascimento", description = "Data de nascimento do paciente", examples = {"10/01/1990"}, format = "date")
    @NotNull
    @PastOrPresent(message = "A data de Nascimento do paciente não pode ser no futuro.")
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataNascimento;

    @Schema(name = "cpf", description = "CPF do paciente", examples = {"123.456.789-00"})
    @CPF
    private String cpf;

    @Schema(name = "rg", description = "RG do paciente", examples = {"200312312334"})
    private String rg;

    @Schema(name = "telefone", description = "Telefone do paciente", examples = {"(11) 91234-5678"})
    private String telefone;

    @Schema(name = "email", description = "Email do paciente", examples = {"jj@gmail.com"})
    @Email
    private String email;

    @Schema(name = "religião", description = "Religião do paciente", examples = {"Católica"})
    private Religiao religiao;

    @Schema(name = "naturalidade", description = "Naturalidade do paciente", examples = {"Brasil"})
    private Naturalidade naturalidade;

    @Schema(name = "escolaridade", description = "Escolaridade do paciente", examples = {"SUPERIOR"})
    private Escolaridade escolaridade;

    @Schema(name = "profissão", description = "Profissão do paciente", examples = {"Engenheiro"})
    private String profissao;

    @Schema(name = "estadoCivil", description = "Estado Civil do paciente", examples = {"SOLTEIRO"})
    private EstadoCivil estadoCivil;

    @Schema(name = "usuarioId", description = "Id do usuário associado ao paciente", examples = {"1"})
    private Long usuarioId;

    private EnderecoRequest endereco;

    private List<FiliacaoRequest> responsaveis;

}

