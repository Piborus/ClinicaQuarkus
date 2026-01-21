package br.ce.clinica.dto.response;

import br.ce.clinica.entity.Paciente;
import br.ce.clinica.enums.Sexo;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PacienteResponse {

    private Long id;

    private String nome;

    private Integer idade;

    private Sexo sexo;

    private LocalDate dataNascimento;

    private String cpf;

    private String rg;

    private String telefone;

    private String email;

    private EnderecoResponse endereco;

    public static PacienteResponse fromEntity(Paciente paciente) {
        return PacienteResponse.builder()
                .id(paciente.getId())
                .nome(paciente.getNome())
                .idade(paciente.getIdade())
                .sexo(paciente.getSexo())
                .dataNascimento(paciente.getDataNascimento())
                .cpf(paciente.getCpf())
                .rg(paciente.getRg())
                .telefone(paciente.getTelefone())
                .email(paciente.getEmail())
                .endereco(EnderecoResponse.fromEntity(paciente.getEndereco()))
                .build();
    }
}
