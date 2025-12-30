package br.ce.clinica.dto.response;

import br.ce.clinica.entity.RelatorioDoPaciente;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioDoPacienteResponse {

    private Long id;

    private String relatorio;

    private PacienteResponse paciente;

    public static RelatorioDoPacienteResponse fromEntity(RelatorioDoPaciente relatorioDoPaciente) {
        return RelatorioDoPacienteResponse.builder()
                .id(relatorioDoPaciente.getId())
                .relatorio(relatorioDoPaciente.getRelatorio())
                .paciente(PacienteResponse.fromEntity(relatorioDoPaciente.getPaciente()))
                .build();
    }
}
