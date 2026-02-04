package br.ce.clinica.dto.response;

import br.ce.clinica.entity.Prontuario;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProntuarioResponse {

    private Long id;

    private String texto;

//    private Long pacienteId;
//
//    private String pacienteNome;

    public static ProntuarioResponse toResponse(Prontuario entity) {
        return ProntuarioResponse.builder()
                .id(entity.getId())
                .texto(entity.getTexto())
//                .pacienteId((entity.getPaciente() != null ? entity.getPaciente().getId() : null))
//                .pacienteNome((entity.getPaciente() != null ? entity.getPaciente().getNome() : null))
                .build();
    }
}
