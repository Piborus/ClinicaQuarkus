package br.ce.clinica.dto.response;

import br.ce.clinica.entity.Relatorio;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioResponse {

    private Long id;

    private String texto;

    private PacienteResponse paciente;

    public static RelatorioResponse toResponse(Relatorio entity) {
        return RelatorioResponse.builder()
                .id(entity.getId())
                .texto(entity.getTexto())
                .paciente(PacienteResponse.toResponse(entity.getPaciente()))
                .build();
    }
}
