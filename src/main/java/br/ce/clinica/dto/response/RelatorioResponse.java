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

    private Long pacienteId;

    private String pacienteNome;

    public static RelatorioResponse toResponse(Relatorio entity) {
        return RelatorioResponse.builder()
                .id(entity.getId())
                .texto(entity.getTexto())
                .pacienteId((entity.getPaciente() != null ? entity.getPaciente().getId() : null))
                .pacienteNome((entity.getPaciente() != null ? entity.getPaciente().getNome() : null))
                .build();
    }
}
