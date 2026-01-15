package br.ce.clinica.dto.request;

import br.ce.clinica.enums.Sexo;
import br.ce.clinica.validation.ValidCpf;
import io.smallrye.common.constraint.NotNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
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
    @NotNull
    @Size(max = 100)
    private String nome;

    @Schema(name = "idade", description = "Idade do paciente", examples = {"30"} )
    @Size(max = 3)
    private Integer idade;

    @Schema(name = "sexo", description = "Sexo do paciente", examples = {"MASCULINO"})
    private Sexo sexo;

    @Schema(name = "dataNascimento", description = "Data de nascimento do paciente", examples = {"1990-01-01"}, format = "date")
    @NotNull
    @PastOrPresent(message = "Data de Nascimento não pode ser futura.")
    private LocalDate dataNascimento;

    @Schema(name = "cpf", description = "CPF do paciente", examples = {"12345678900"})
    @ValidCpf
    private String cpf;

    @Schema(name = "rg", description = "RG do paciente", examples = {"200312312334"})
    private String rg;

    @Schema(name = "telefone", description = "Telefone do paciente", examples = {"(11) 91234-5678"})
    private String telefone;

    @Schema(name = "email", description = "Email do paciente", examples = {"jj@gmail.com"})
    @Email
    private String email;

    private EnderecoRequest endereco;
}

