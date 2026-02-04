package br.ce.clinica.dto.response;

import br.ce.clinica.entity.Prontuario;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProntuarioResumeResponse {

    private Long id;

    private String texto;

    public static ProntuarioResumeResponse toResponse(Prontuario prontuario) {
        return ProntuarioResumeResponse.builder()
                .id(prontuario.getId())
                .texto(prontuario.getTexto())
                .build();
    }
}
