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

    public static RelatorioDoPacienteResponse toResponse(RelatorioDoPaciente entity) {
        return RelatorioDoPacienteResponse.builder()
                .id(entity.getId())
                .relatorio(entity.getRelatorio())
                .build();
    }

    public static RelatorioDoPacienteResponse toDetailedResponse(RelatorioDoPaciente entity) {
        return RelatorioDoPacienteResponse.builder()
                .id(entity.getId())
                .relatorio(entity.getRelatorio())
                .paciente(PacienteResponse.fromEntity(entity.getPaciente()))
                .build();
    }

}
