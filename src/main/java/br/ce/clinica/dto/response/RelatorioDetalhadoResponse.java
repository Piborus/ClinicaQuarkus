package br.ce.clinica.dto.response;

import br.ce.clinica.entity.Relatorio;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelatorioDetalhadoResponse {

    private Long id;

    private String texto;

    private PacienteResponse paciente;

    public static RelatorioDetalhadoResponse toDetailedResponse(Relatorio entity) {
        return RelatorioDetalhadoResponse.builder()
                .id(entity.getId())
                .texto(entity.getTexto())
                .paciente(PacienteResponse.fromEntity(entity.getPaciente()))
                .build();
    }
}
