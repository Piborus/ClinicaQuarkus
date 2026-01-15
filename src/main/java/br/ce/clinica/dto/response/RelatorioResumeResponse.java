package br.ce.clinica.dto.response;

import br.ce.clinica.entity.Relatorio;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RelatorioResumeResponse {

    private Long id;

    private String texto;

    public static RelatorioResumeResponse toResponse(Relatorio relatorio) {
        return RelatorioResumeResponse.builder()
                .id(relatorio.getId())
                .texto(relatorio.getTexto())
                .build();
    }
}
