package br.ce.clinica.dto.response;

import br.ce.clinica.entity.Prontuario;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProntuarioResponse {

    private Long id;

    private String texto;

    public static ProntuarioResponse toResponse(Prontuario entity) {
        return ProntuarioResponse.builder()
                .id(entity.getId())
                .texto(entity.getTexto())
                .build();
    }
}
