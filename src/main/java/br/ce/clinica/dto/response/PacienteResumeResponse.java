package br.ce.clinica.dto.response;

import br.ce.clinica.entity.Paciente;
import br.ce.clinica.enums.Sexo;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PacienteResumeResponse {

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

    private List<CarteiraResumeResponse> transacoes;

    private List<ProntuarioResumeResponse> prontuarios;

    public static PacienteResumeResponse toResponse(Paciente paciente) {
        return PacienteResumeResponse.builder()
                .id(paciente.getId())
                .nome(paciente.getNome())
                .idade(paciente.getIdade())
                .sexo(paciente.getSexo())
                .dataNascimento(paciente.getDataNascimento())
                .cpf(paciente.getCpf())
                .rg(paciente.getRg())
                .telefone(paciente.getTelefone())
                .email(paciente.getEmail())
                .endereco(EnderecoResponse.toResponse(paciente.getEndereco()))
                .transacoes(paciente.getTransacao().stream()
                        .map(CarteiraResumeResponse::toResponse)
                        .toList())
                .prontuarios(paciente.getProntuarioDoPaciente().stream()
                        .map(ProntuarioResumeResponse::toResponse)
                        .toList())
                .build();
    }
}
