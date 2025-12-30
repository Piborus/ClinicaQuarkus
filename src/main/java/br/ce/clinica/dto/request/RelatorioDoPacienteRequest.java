package br.ce.clinica.dto.request;

import lombok.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RelatorioDoPacienteRequest {

    @Schema(name = "relatorio", description = "Relatório do paciente", examples = { "Paciente apresenta melhora significativa após o tratamento." })
    private String relatorio;

    @Schema(name = "pacienteId", description = "Id do paciente", examples = { "1" })
    private Long pacienteId;

}
