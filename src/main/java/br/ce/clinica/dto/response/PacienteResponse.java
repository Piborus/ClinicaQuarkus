package br.ce.clinica.dto.response;

import br.ce.clinica.entity.Paciente;
import br.ce.clinica.enums.Sexo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PacienteResponse {

    private Long id;

    private String nome;

    private Integer idade;

    private Sexo sexo;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataNascimento;

    private String cpf;

    private String rg;

    private String telefone;

    private String email;

    private EnderecoResponse endereco;

    private List<CarteiraResponse> transacoes;

    private List<ProntuarioResponse> prontuarios;

    private List<FiliacaoResponse> responsaveis;

    public static PacienteResponse toResponse(Paciente paciente) {
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
                .endereco(EnderecoResponse.toResponse(paciente.getEndereco()))
                .transacoes(
                        paciente.getTransacao() == null
                                ? List.of()
                                : paciente.getTransacao().stream()
                                .map(CarteiraResponse::toResponse)
                                .toList()
                )
                .prontuarios(
                        paciente.getProntuarioDoPaciente() == null
                                ? List.of()
                                : paciente.getProntuarioDoPaciente().stream()
                                .map(ProntuarioResponse::toResponse)
                                .toList()
                )
                .responsaveis(
                        paciente.getResponsaveis() == null
                                ? List.of()
                                : paciente.getResponsaveis().stream()
                                .map(FiliacaoResponse::toResponse)
                                .toList()
                )
                .build();
    }
}
